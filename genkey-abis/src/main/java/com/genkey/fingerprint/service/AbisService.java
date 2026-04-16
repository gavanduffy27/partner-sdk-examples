package com.genkey.fingerprint.service;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.fingerprint.config.AbisConfig;
import com.genkey.fingerprint.model.*;
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
            return EnrollmentResponse.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            BiographicService biographicService = DGIEServiceModule.getBiographicService();
            
            String subjectId = request.getSubjectId();
            
            // Check if subject already exists
            if (abisService.existsSubject(subjectId)) {
                return EnrollmentResponse.builder()
                        .success(false)
                        .subjectId(subjectId)
                        .statusCode(409)
                        .statusMessage("Subject already exists")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Create enrollment reference
            SubjectEnrollmentReference enrollmentRef = new SubjectEnrollmentReference(subjectId);
            if (request.getTargetFingers() != null && request.getTargetFingers().length > 0) {
                enrollmentRef.setTargetFingers(request.getTargetFingers());
            } else if (request.getFingerprints() != null && !request.getFingerprints().isEmpty()) {
                int[] inferredTargets = request.getFingerprints().stream().mapToInt(FingerprintData::getFinger).toArray();
                enrollmentRef.setTargetFingers(inferredTargets);
            }
            if (request.getFaceImage() != null && request.getFaceImage().length > 0) {
                String faceFormat = request.getFaceImageFormat() != null ? request.getFaceImageFormat() : "JPG";
                log.info("Setting face portrait for enrollment subject={}, format={}, bytes={}",
                        subjectId, faceFormat, request.getFaceImage().length);
                enrollmentRef.setFacePortrait(new ImageBlob(request.getFaceImage(), faceFormat));
            }

            if ((request.getFingerprints() == null || request.getFingerprints().isEmpty()) &&
                    (request.getFaceImage() == null || request.getFaceImage().length == 0)) {
                return EnrollmentResponse.builder()
                        .success(false)
                        .subjectId(subjectId)
                        .statusCode(400)
                        .statusMessage("Enrollment failed: No fingerprint or face data provided")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Process each fingerprint
            if (request.getFingerprints() != null) {
                for (FingerprintData fpData : request.getFingerprints()) {
                // Create finger reference - DISABLE autoExtract to prevent native crashes
                FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(fpData.getFinger(), false);
                
                // Convert format string to SDK format code for addImage()
                String imageFormat = getImageFormatCode(fpData.getImageFormat());
                int resolution = fpData.getResolution() > 0 ? fpData.getResolution() : 500;
                
                log.info("Adding enrollment fingerprint: finger={}, resolution={}, dataSize={}", 
                         fpData.getFinger(), resolution,
                         fpData.getImageData() != null ? fpData.getImageData().length : 0);
                
                // Use ImageData wrapper and addImageData() instead of direct addImage()
                try {
                    ImageData imageData;
                    String originalFormat = fpData.getImageFormat();
                    if ("RAW".equalsIgnoreCase(originalFormat)) {
                        // Use raw constructor for (width, height, data, resolution)
                        imageData = new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), resolution);
                    } else {
                        // Use default constructor for encoded data
                        imageData = new ImageData(fpData.getImageData(), imageFormat, resolution);
                    }
                    // Always use WSQ as the supported storage format constant for addImageData
                    fingerRef.addImageData(imageData, ImageData.FORMAT_WSQ);
                } catch (Exception inner) {
                    log.error("Failed to add image for finger {}: {}", fpData.getFinger(), inner.getMessage(), inner);
                    throw new RuntimeException("Enrollment failed for finger " + fpData.getFinger(), inner);
                }
                enrollmentRef.add(fingerRef);
                }
            }
            
            
            // Perform enrollment
            MatchEngineResponse response = abisService.insertSubject(enrollmentRef, false);
            
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
            return com.genkey.fingerprint.model.VerifyResponse.builder()
                    .verified(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            String subjectId = request.getSubjectId();
            
            boolean subjectExists = true;
            if (request.getFaceImage() != null && request.getFaceImage().length > 0 &&
                    (request.getFingerprints() == null || request.getFingerprints().isEmpty())) {
                log.info("Face-only verify request for subject {}: skipping subject exists check", subjectId);
            } else {
                subjectExists = abisService.existsSubject(subjectId);
            }
            
            if (!subjectExists) {
                return com.genkey.fingerprint.model.VerifyResponse.builder()
                        .verified(false)
                        .subjectId(subjectId)
                        .statusCode(404)
                        .statusMessage("Subject not found")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Create verification reference
            SubjectEnrollmentReference verifyRef = new SubjectEnrollmentReference(subjectId);
            if (request.getTargetFingers() != null && request.getTargetFingers().length > 0) {
                verifyRef.setTargetFingers(request.getTargetFingers());
            } else if (request.getFingerprints() != null && !request.getFingerprints().isEmpty()) {
                verifyRef.setTargetFingers(request.getFingerprints().stream().mapToInt(FingerprintData::getFinger).toArray());
            }
            if (request.getFaceImage() != null && request.getFaceImage().length > 0) {
                String faceFormat = request.getFaceImageFormat() != null ? request.getFaceImageFormat() : "JPG";
                verifyRef.setFacePortrait(new ImageBlob(request.getFaceImage(), faceFormat));
            }

            if ((request.getFingerprints() == null || request.getFingerprints().isEmpty()) &&
                    (request.getFaceImage() == null || request.getFaceImage().length == 0)) {
                return com.genkey.fingerprint.model.VerifyResponse.builder()
                        .verified(false)
                        .subjectId(subjectId)
                        .statusCode(400)
                        .statusMessage("Verification failed: No fingerprint or face data provided")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Process fingerprints if provided
            if (request.getFingerprints() != null) {
                for (FingerprintData fpData : request.getFingerprints()) {
                    FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(fpData.getFinger(), false);
                    String imageFormat = getImageFormatCode(fpData.getImageFormat());
                    int resolution = fpData.getResolution() > 0 ? fpData.getResolution() : 500;
                    
                    try {
                        ImageData imageData;
                        if ("RAW".equalsIgnoreCase(fpData.getImageFormat())) {
                            imageData = new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), resolution);
                        } else {
                            imageData = new ImageData(fpData.getImageData(), imageFormat, resolution);
                        }
                        fingerRef.addImageData(imageData, ImageData.FORMAT_WSQ);
                    } catch (Exception e) {
                        log.error("Failed to add image for verification: {}", e.getMessage());
                        throw new RuntimeException("Verification setup failed", e);
                    }
                    verifyRef.add(fingerRef);
                }
            }
            
            // Perform verification
            VerifyResponse verifyResponse = abisService.verifySubject(verifyRef);
            
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
            return IdentifyResponse.builder()
                    .found(false)
                    .statusCode(-1)
                    .statusMessage("ABIS Service not initialized")
                    .build();
        }
        
        try {
            GenkeyABISService abisService = ABISServiceModule.getABISService();
            
            // Create identification reference
            SubjectEnrollmentReference identifyRef = new SubjectEnrollmentReference();
            if (request.getTargetFingers() != null && request.getTargetFingers().length > 0) {
                identifyRef.setTargetFingers(request.getTargetFingers());
            } else if (request.getFingerprints() != null && !request.getFingerprints().isEmpty()) {
                identifyRef.setTargetFingers(request.getFingerprints().stream().mapToInt(FingerprintData::getFinger).toArray());
            }
            if (request.getFaceImage() != null && request.getFaceImage().length > 0) {
                String faceFormat = request.getFaceImageFormat() != null ? request.getFaceImageFormat() : "JPG";
                identifyRef.setFacePortrait(new ImageBlob(request.getFaceImage(), faceFormat));
            }

            if ((request.getFingerprints() == null || request.getFingerprints().isEmpty()) &&
                    (request.getFaceImage() == null || request.getFaceImage().length == 0)) {
                return IdentifyResponse.builder()
                        .found(false)
                        .statusCode(400)
                        .statusMessage("Identification failed: No fingerprint or face data provided")
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Process fingerprints
            if (request.getFingerprints() != null) {
                for (FingerprintData fpData : request.getFingerprints()) {
                    // Disable autoExtract for identification to prevent native crashes
                    FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(fpData.getFinger(), false);
                
                // Convert format string to SDK format code
                String imageFormat = getImageFormatCode(fpData.getImageFormat());
                int resolution = fpData.getResolution() > 0 ? fpData.getResolution() : 500;
                
                log.info("Adding identification fingerprint: finger={}, resolution={}, dataSize={}", 
                         fpData.getFinger(), resolution, 
                         fpData.getImageData() != null ? fpData.getImageData().length : 0);
                
                // Use ImageData wrapper and addImageData() instead of direct addImage()
                try {
                    ImageData imageData;
                    String originalFormat = fpData.getImageFormat();
                    if ("RAW".equalsIgnoreCase(originalFormat)) {
                        // Use raw constructor
                        imageData = new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), resolution);
                    } else {
                        // Use default constructor
                        imageData = new ImageData(fpData.getImageData(), imageFormat, resolution);
                    }
                    // Always use WSQ as the supported storage format constant for addImageData
                    fingerRef.addImageData(imageData, ImageData.FORMAT_WSQ);
                } catch (Exception e) {
                    log.error("Failed to add image for identification: {}", e.getMessage(), e);
                    throw new RuntimeException("Identification setup failed", e);
                }
                identifyRef.add(fingerRef);
                }
            }
            
            
            // Perform identification (1:N search) using querySubject
            MatchEngineResponse response = abisService.querySubject(identifyRef, false);
            
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
    
    /**
     * Convert format string to ABIS SDK integer format code.
     * The addImage() method expects integer format constants: ImageData.FORMAT_BMP or ImageData.FORMAT_WSQ
     * Other formats will default to ImageData.FORMAT_BMP.
     */
    private String getImageFormatCode(String format) {
        if (format == null) {
            return ImageData.FORMAT_BMP; // Default to BMP format
        }
        String upperFormat = format.toUpperCase();
        if ("RAW".equals(upperFormat)) {
            return ImageData.FORMAT_WSQ;
        }
        switch (upperFormat) {
            case "WSQ":
                return ImageData.FORMAT_WSQ;
            case "BMP":
            case "PNG":
            case "JPEG":
            case "JPG":
            default:
                // SDK constant for BMP
                return ImageData.FORMAT_BMP;
        }
    }
}
