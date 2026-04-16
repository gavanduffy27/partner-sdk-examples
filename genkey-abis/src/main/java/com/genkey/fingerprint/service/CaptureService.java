package com.genkey.fingerprint.service;

import com.genkey.fingerprint.config.AbisConfig;
import com.genkey.fingerprint.model.*;
import com.genkey.fingerprint.scanner.FingerprintScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Service for capturing fingerprints from scanner hardware
 * and processing them through the ABIS system.
 */
@Slf4j
@Service
public class CaptureService {
    
    private final FingerprintScanner scanner;
    private final AbisService abisService;
    private final AbisConfig config;
    
    public CaptureService(FingerprintScanner scanner, AbisService abisService, AbisConfig config) {
        this.scanner = scanner;
        this.abisService = abisService;
        this.config = config;
    }
    
    /**
     * Check if scanner is ready for capture
     */
    public boolean isScannerReady() {
        return scanner.isReady();
    }
    
    /**
     * Get scanner information
     */
    public String getScannerInfo() {
        return scanner.getDeviceInfo();
    }
    
    /**
     * Capture a single fingerprint
     */
    public CaptureResult captureSingleFinger(int finger) {
        log.info("Capturing fingerprint for finger: {}", finger);
        
        if (!scanner.isReady()) {
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not ready")
                    .finger(finger)
                    .build();
        }
        
        int timeout = config.getCapture().getTimeout();
        int maxRetries = config.getCapture().getMaxRetries();
        int qualityThreshold = config.getCapture().getQualityThreshold();
        
        CaptureResult result = null;
        
        CaptureResult bestResult=null;
        
        // GD Modified this loop to maintain the best result
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Capture attempt {} of {} for finger {}", attempt, maxRetries, finger);
            
            result = scanner.capture(finger, timeout);
            
            if (result.isSuccess() && result.getQuality() >= qualityThreshold) {
                log.info("Capture successful with quality: {}", result.getQuality());
                return result;
            }
            
            if (result.isSuccess() && result.getQuality() < qualityThreshold) {
                log.warn("Quality {} below threshold {}, retrying...", 
                        result.getQuality(), qualityThreshold);
            } else {
                log.warn("Capture failed: {}", result.getStatusMessage());
            }
            if (bestResult == null || result.getQuality() > bestResult.getQuality()) {
            	bestResult=result;
            }
        }
        
        // Return BEST not last result even if quality is low
        if (bestResult != null && bestResult.isSuccess()) {
            log.warn("Returning capture with quality {} (below threshold {})", 
            		bestResult.getQuality(), qualityThreshold);
        }
        
        return bestResult;
    }
    
    
    public MultipleCaptureResult captureMultipleFingerImage(int [] fingers) {
    	
    	return scanner.captureMultiple(fingers, config.getCapture().getTimeout());
    }
    
    
    /**
     * Replacement function for managing a segment image based on raw image capture and
     * a segmentation.
     * @param fingers
     * @return
     */
    public List<CaptureResult> captureMultipleFingers(int[] fingers) {
    	MultipleCaptureResult captureResult = captureMultipleFingerImage(fingers);
    	List<CaptureResult> result = CaptureUtils.segmentCaptureResult(captureResult, captureResult.getFingers());
    	return result;
    }
    
    
    /**
     * Capture multiple fingerprints
     */
    public List<CaptureResult> captureMultipleFingersOld(int[] fingers) {
        List<CaptureResult> results = new ArrayList<>();
        
        for (int finger : fingers) {
            CaptureResult result = captureSingleFinger(finger);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Capture and enroll a subject
     */
    public EnrollmentResponse captureAndEnroll(String subjectId, String firstName, 
            String lastName, int[] fingers) {
        
        log.info("Starting capture and enroll for subject: {}", subjectId);
        
        // Check if subject already exists
        if (abisService.subjectExists(subjectId)) {
            return EnrollmentResponse.builder()
                    .success(false)
                    .subjectId(subjectId)
                    .statusCode(409)
                    .statusMessage("Subject already exists")
                    .build();
        }
        
        // Capture fingerprints
        List<FingerprintData> fingerprints = new ArrayList<>();
        
        for (int finger : fingers) {
            log.info("------------------------------------------------------------");
            log.info("STARTING CYCLE FOR FINGER: {} ({})", finger, getFingerName(finger));
            log.info("Please place finger {} on the scanner...", getFingerName(finger));
            
            CaptureResult captureResult = captureSingleFinger(finger);
            
            if (!captureResult.isSuccess()) {
                log.error("Failed to capture finger {}: {}", finger, captureResult.getStatusMessage());
                continue;
            }
            
            fingerprints.add(FingerprintData.builder()
                    .finger(finger)
                    .imageData(captureResult.getImageData())
                    .imageFormat(captureResult.getImageFormat())
                    .quality(captureResult.getQuality())
                    .width(captureResult.getWidth())
                    .height(captureResult.getHeight())
                    .resolution(captureResult.getResolution())
                    .build());
        }
        
        if (fingerprints.isEmpty()) {
            return EnrollmentResponse.builder()
                    .success(false)
                    .subjectId(subjectId)
                    .statusCode(-3)
                    .statusMessage("No fingerprints captured successfully")
                    .build();
        }
        
        // Create enrollment request
        EnrollmentRequest request = EnrollmentRequest.builder()
                .subjectId(subjectId)
                .firstName(firstName)
                .lastName(lastName)
                .targetFingers(fingers)
                .fingerprints(fingerprints)
                .build();
        
        // Perform enrollment
        return abisService.enrollSubject(request);
    }
    
    /**
     * Capture and verify a subject
     */
    public VerifyResponse captureAndVerify(String subjectId, int[] fingers) {
        log.info("Starting capture and verify for subject: {}", subjectId);
        
        // Capture fingerprints
        List<FingerprintData> fingerprints = new ArrayList<>();
        
        for (int finger : fingers) {
            log.info("Please place finger {} on the scanner...", getFingerName(finger));
            
            CaptureResult captureResult = captureSingleFinger(finger);
            
            if (!captureResult.isSuccess()) {
                log.error("Failed to capture finger {}", finger);
                continue;
            }
            
            fingerprints.add(FingerprintData.builder()
                    .finger(finger)
                    .imageData(captureResult.getImageData())
                    .imageFormat(captureResult.getImageFormat())
                    .quality(captureResult.getQuality())
                    .width(captureResult.getWidth())
                    .height(captureResult.getHeight())
                    .resolution(captureResult.getResolution())
                    .build());
        }
        
        if (fingerprints.isEmpty()) {
            return VerifyResponse.builder()
                    .verified(false)
                    .subjectId(subjectId)
                    .statusCode(-3)
                    .statusMessage("No fingerprints captured successfully")
                    .build();
        }
        
        // Create verify request
        VerifyRequest request = VerifyRequest.builder()
                .subjectId(subjectId)
                .targetFingers(fingers)
                .fingerprints(fingerprints)
                .build();
        
        return abisService.verifySubject(request);
    }
    
    /**
     * Capture and identify (1:N search)
     */
    public IdentifyResponse captureAndIdentify(int[] fingers, int maxCandidates) {
        log.info("Starting capture and identify...");
        
        // Capture fingerprints
        List<FingerprintData> fingerprints = new ArrayList<>();
        
        for (int finger : fingers) {
            log.info("Please place finger {} on the scanner...", getFingerName(finger));
            
            CaptureResult captureResult = captureSingleFinger(finger);
            
            if (!captureResult.isSuccess()) {
                log.error("Failed to capture finger {}", finger);
                continue;
            }
            
            fingerprints.add(FingerprintData.builder()
                    .finger(finger)
                    .imageData(captureResult.getImageData())
                    .imageFormat(captureResult.getImageFormat())
                    .quality(captureResult.getQuality())
                    .width(captureResult.getWidth())
                    .height(captureResult.getHeight())
                    .resolution(captureResult.getResolution())
                    .build());
        }
        
        if (fingerprints.isEmpty()) {
            return IdentifyResponse.builder()
                    .found(false)
                    .statusCode(-3)
                    .statusMessage("No fingerprints captured successfully")
                    .build();
        }
        
        // Create identify request
        IdentifyRequest request = IdentifyRequest.builder()
                .targetFingers(fingers)
                .fingerprints(fingerprints)
                .maxCandidates(maxCandidates)
                .build();
        
        return abisService.identifySubject(request);
    }
    
    /**
     * Get human-readable finger name
     */
    public static String getFingerName(int finger) {
        return switch (finger) {
            case 1 -> "Right Thumb";
            case 2 -> "Right Index";
            case 3 -> "Right Middle";
            case 4 -> "Right Ring";
            case 5 -> "Right Little";
            case 6 -> "Left Thumb";
            case 7 -> "Left Index";
            case 8 -> "Left Middle";
            case 9 -> "Left Ring";
            case 10 -> "Left Little";
            default -> "Unknown Finger " + finger;
        };
    }
}
