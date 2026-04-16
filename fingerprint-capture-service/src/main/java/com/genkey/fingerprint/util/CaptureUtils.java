package com.genkey.fingerprint.util;

import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for processing fingerprint capture results
 */
@Slf4j
public class CaptureUtils {
    
    /**
     * Segment a multiple finger capture result into individual finger results
     * Uses individual finger images from the fingerImages array if available
     * 
     * @param multipleResult the multiple finger capture result
     * @param fingers array of finger positions
     * @return list of individual capture results
     */
    public static List<CaptureResult> segmentCaptureResult(MultipleFingerCaptureResult multipleResult, int[] fingers) {
        List<CaptureResult> results = new ArrayList<>();
        
        if (multipleResult == null || fingers == null) {
            log.warn("Invalid input for segmentCaptureResult");
            return results;
        }
        
        byte[][] fingerImages = multipleResult.getFingerImages();
        
        // Create individual finger results using finger-specific images if available
        for (int i = 0; i < fingers.length; i++) {
            int finger = fingers[i];
            
            // Use individual finger image if available, otherwise fall back to the single image
            byte[] imageData = null;
            if (fingerImages != null && i < fingerImages.length && fingerImages[i] != null) {
                imageData = fingerImages[i];
                log.debug("Using individual image for finger {} (index {})", finger, i);
            } else {
                imageData = multipleResult.getImageData();
                log.debug("Using shared image for finger {} (individual image not available)", finger);
            }
            
            CaptureResult result = CaptureResult.builder()
                    .success(multipleResult.isSuccess())
                    .statusCode(multipleResult.getStatusCode())
                    .statusMessage(multipleResult.getStatusMessage())
                    .finger(finger)
                    .imageData(imageData)
                    .imageFormat(multipleResult.getImageFormat())
                    .quality(multipleResult.getQuality())
                    .width(multipleResult.getWidth())
                    .height(multipleResult.getHeight())
                    .resolution(multipleResult.getResolution())
                    .captureTimeMs(multipleResult.getCaptureTimeMs())
                    .build();
            
            results.add(result);
        }
        
        log.info("Segmented multiple capture result into {} individual finger results", results.size());
        return results;
    }
}
