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
public class VerifyResponse {
    
    private boolean verified;
    private String subjectId;
    private double matchScore;
    private Double faceScore; // Face matching score (null if not used)
    private int statusCode;
    private String statusMessage;
    private List<FingerMatchDetail> fingerDetails;
    private long processingTimeMs;
}
