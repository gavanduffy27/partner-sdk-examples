package com.genkey.fingerprint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyRequest {
    
    private int[] targetFingers;
    private List<FingerprintData> fingerprints;
    
    // FaceMatch support
    private byte[] faceImage;
    private String faceImageFormat;
    
    private int maxCandidates;
    private double threshold;
}
