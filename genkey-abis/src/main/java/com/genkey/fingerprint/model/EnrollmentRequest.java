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
public class EnrollmentRequest implements BiometricRequest {
    
    @NotBlank(message = "Subject ID is required")
    private String subjectId;
    
    private String firstName;
    private String lastName;
    private String gender;
    private String dateOfBirth;
    
    // Target fingers is optional for face-only enrollment. Validate finger counts in service logic.
    private int[] targetFingers;
    
    private List<FingerprintData> fingerprints;
    
    private boolean includeFace;
    private byte[] faceImage;
    private String faceImageFormat;
}
