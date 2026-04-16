package com.genkey.fingerprint.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {
    
    @NotBlank(message = "Subject ID is required")
    private String subjectId;
    
    private int[] targetFingers;
    private List<FingerprintData> fingerprints;
    
    // FaceMatch support
    private byte[] faceImage;
    private String faceImageFormat;
}
