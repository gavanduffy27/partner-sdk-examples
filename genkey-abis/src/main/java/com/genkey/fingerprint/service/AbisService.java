package com.genkey.fingerprint.service;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.fingerprint.config.AbisConfig;
import com.genkey.fingerprint.controller.EnrollmentDataContainer;
import com.genkey.fingerprint.model.*;
import com.genkey.fingerprint.util.CaptureUtils;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for interacting with the GenKey ABIS system.
 * Handles enrollment, verification, and identification operations.
 */
@Slf4j
@Service
public class AbisService {
    
    private final AbisConfig config;
    private boolean initialized = false;
    
    public AbisService(AbisConfig config) {
        this.config = config;
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing ABIS Service...");
        
        try {
            // Set library path
            System.setProperty("java.library.path", config.getLibrary().getPath());
            
            // Load ABIS libraries
            if (!ABISClientLibrary.isInitialized()) {
                ABISClientLibrary.loadLibraries();
                ABISClientLibrary.initializeDefault();
            }
            
            // Configure REST services using singleton instance
            String host = config.getServer().getHost();
            int port = config.getServer().getPort();
            String domain = config.getServer().getDomain();
            
            RestServices.getInstance().setABISPort(port);
            
            // Initialize services using DGIEServiceModule (correct pattern from SDK)
            DGIEServiceModule.initCoreServices(host, port, domain);
            
            initialized = true;
            log.info("ABIS Service initialized successfully");
            log.info("Connected to ABIS server at {}:{}", host, port);
            
        } catch (Exception e) {
            log.error("Failed to initialize ABIS Service", e);
            initialized = false;
        }
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ABIS Service...");
        try {
            ImageContextSDK.shutdownLibrary();
        } catch (Exception e) {
            log.error("Error during ABIS shutdown", e);
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Test connection to ABIS server
     */
    public String testConnection() {
        if (!initialized) {
            return null;
        }
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            return abisService.testABISConnection();
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return null;
        }
    }
    
    /**
     * Check if a subject exists in the system
     */
    public boolean subjectExists(String subjectId) {
        if (!initialized) return false;
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            return abisService.existsSubject(subjectId);
        } catch (Exception e) {
            log.error("Failed to check subject existence", e);
            return false;
        }
    }
    
    /**
     * Enroll a subject with fingerprint data
     */
    public EnrollmentResponse enrollSubject(EnrollmentRequest request) {
        long startTime = System.currentTimeMillis();
        
        if (!initialized) {
        	log.error("Failing because system not initialised");
            return EnrollmentResponse.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }

        String subjectId = request.getSubjectId();
        if (CaptureUtils.isNullRequest(request)) {
        	log.error("Failing because of null request for {}", subjectId);
            return EnrollmentResponse.builder()
                    .success(false)
                    .subjectId(subjectId)
                    .statusCode(400)
                    .statusMessage("Enrollment failed: No fingerprint or face data provided")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            BiographicService biographicService = DGIEServiceModule.getBiographicService();
            
            
            // Check if subject already exists
            if (abisService.existsSubject(subjectId)) {
            	log.warn("Exiting call on insert for existing subject {}", subjectId);
                return EnrollmentResponse.builder()
                        .success(false)
                        .subjectId(subjectId)
                        .statusCode(409)
                        .statusMessage("Subject already exists")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            EnrollmentDataContainer container = new EnrollmentDataContainer(subjectId);
            container.addBiometricRequest(request);

            
            // Perform enrollment
            MatchEngineResponse response = abisService.insertSubject(container.getEnrolmentReference(), false);
            
            if (response.isSuccess()) {
                // Create biographic record
                BiographicProfileRecord bioRecord = biographicService.createProfileRecord(subjectId);
                if (request.getFirstName() != null) bioRecord.setFirstName(request.getFirstName());
                if (request.getLastName() != null) bioRecord.setLastName(request.getLastName());
                if (request.getGender() != null) bioRecord.setGender(request.getGender());
                
                biographicService.insertBiographicRecord(bioRecord);
                
                // Check for duplicates
                List<MatchResultInfo> duplicates = new ArrayList<>();
                if (response.hasMatchResults()) {
                    // Process match results for duplicates
                    List<MatchResult> matchResults = response.getMatchResults();
                    if (matchResults != null) {
                        int rank = 1;
                        for (MatchResult result : matchResults) {
                            duplicates.add(MatchResultInfo.builder()
                                    .subjectId(result.getSubjectID())
                                    .matchScore(result.getMatchScore())
                                    .rank(rank++)
                                    .build());
                        }
                    }
                }
                
                return EnrollmentResponse.builder()
                        .success(true)
                        .subjectId(subjectId)
                        .externalId(subjectId)
                        .operationResult(response.getOperationResult())
                        .statusCode(response.getStatusCode())
                        .statusMessage("Enrollment successful")
                        .duplicates(duplicates)
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            } else {
                return EnrollmentResponse.builder()
                        .success(false)
                        .subjectId(subjectId)
                        .statusCode(response.getStatusCode())
                        .statusMessage("Enrollment failed: " + response.getOperationResult())
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Enrollment failed", e);
            return EnrollmentResponse.builder()
                    .success(false)
                    .subjectId(request.getSubjectId())
                    .statusCode(-2)
                    .statusMessage("Enrollment failed: " + e.getMessage())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Verify a subject against enrolled fingerprints
     */
    public com.genkey.fingerprint.model.VerifyResponse verifySubject(VerifyRequest request) {
        long startTime = System.currentTimeMillis();
        
        if (!initialized) {
        	log.error("Exiting on system not initialized");
            return com.genkey.fingerprint.model.VerifyResponse.builder()
                    .verified(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }


        String subjectId = request.getSubjectId();
        if (CaptureUtils.isNullRequest(request)) {
        	log.error("Exiting on null request for {}", subjectId);
            return com.genkey.fingerprint.model.VerifyResponse.builder()
                    .verified(false)
                    .subjectId(subjectId)
                    .statusCode(400)
                    .statusMessage("Verification failed: No fingerprint or face data provided")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
        
            // Note this will also check for face only subjects 
            boolean subjectExists = abisService.existsSubject(subjectId);
                    
            if (!subjectExists) {
            	log.error("Exiting verify on subject not exists for {}", subjectId);
                return com.genkey.fingerprint.model.VerifyResponse.builder()
                        .verified(false)
                        .subjectId(subjectId)
                        .statusCode(400)
                        .statusMessage("Verification failed: No fingerprint or face data provided")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            EnquireStatus enquire = abisService.enquireSubject(subjectId);

            EnrollmentDataContainer container = new EnrollmentDataContainer(subjectId);
            container.addBiometricRequest(request);
            
            // Perform verification
            VerifyResponse verifyResponse = abisService.verifySubject(container.getEnrolmentReference());
            
            double matchScore = 0.0;
            if (verifyResponse != null && verifyResponse.getMatchResult() != null) {
                matchScore = verifyResponse.getMatchResult().getMatchScore();
            }
            
            boolean verified = verifyResponse != null && verifyResponse.getMatchResult() != null && verifyResponse.isVerified();
            String statusMsg = verified ? "Verification successful" : 
                    (verifyResponse != null && verifyResponse.getOperationResult() != null ? 
                    verifyResponse.getOperationResult() : "Verification failed");

            return com.genkey.fingerprint.model.VerifyResponse.builder()
                    .verified(verified)
                    .subjectId(subjectId)
                    .matchScore(matchScore)
                    .statusCode(verifyResponse != null ? verifyResponse.getStatusCode() : -1)
                    .statusMessage(statusMsg)
                    .fingerDetails(new ArrayList<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Verification failed", e);
            return com.genkey.fingerprint.model.VerifyResponse.builder()
                    .verified(false)
                    .subjectId(request.getSubjectId())
                    .statusCode(-2)
                    .statusMessage("Verification failed: " + e.getMessage())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Identify a subject by fingerprints (1:N search)
     */
    public IdentifyResponse identifySubject(IdentifyRequest request) {
        long startTime = System.currentTimeMillis();
        
        if (!initialized) {
        	log.error("Exiting on system not initialized");
            return IdentifyResponse.builder()
                    .found(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }
        
        if (CaptureUtils.isNullRequest(request)) {
        	log.error("Exoting on null request");
            return IdentifyResponse.builder()
                    .found(false)
                    .statusCode(400)
                    .statusMessage("Identification failed: No fingerprint or face data provided")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();        	
        }
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();

            EnrollmentDataContainer container = new EnrollmentDataContainer();
            container.addBiometricRequest(request);
            
            // Perform identification (1:N search) using querySubject
            log.info("Sending query request");
            MatchEngineResponse response = abisService.querySubject(container.getEnrolmentReference());
            List<MatchResultInfo> candidates = new ArrayList<>();
            if (response.hasMatchResults()) {
                List<MatchResult> matchResults = response.getMatchResults();
                if (matchResults != null) {
                    int rank = 1;
                    for (MatchResult result : matchResults) {
                        candidates.add(MatchResultInfo.builder()
                                .subjectId(result.getSubjectID())
                                .matchScore(result.getMatchScore())
                                .rank(rank++)
                                .build());
                    }
                }
            }
            
            return IdentifyResponse.builder()
                    .found(!candidates.isEmpty())
                    .candidateCount(candidates.size())
                    .candidates(candidates)
                    .statusCode(response.getStatusCode())
                    .statusMessage(candidates.isEmpty() ? "No matches found" : "Matches found")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Identification failed with exception: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace: ", e);
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            return IdentifyResponse.builder()
                    .found(false)
                    .statusCode(-2)
                    .statusMessage("Identification failed: " + errorMsg)
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Delete a subject from the system
     */
    public boolean deleteSubject(String subjectId) {
        if (!initialized) return false;
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            BiographicService biographicService = DGIEServiceModule.getBiographicService();
            
            // Delete biometric data
            abisService.deleteSubject(subjectId, true);
            
            // Delete biographic record
            if (biographicService.existsBiographicRecord(subjectId)) {
                biographicService.deleteBiographicRecord(subjectId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to delete subject", e);
            return false;
        }
    }
    
}
