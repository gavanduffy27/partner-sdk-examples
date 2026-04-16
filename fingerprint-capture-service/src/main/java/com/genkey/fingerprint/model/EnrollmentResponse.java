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
public class EnrollmentResponse {
    
    private boolean success;
    private String subjectId;
    private String externalId;
    private String operationResult;
    private int statusCode;
    private String statusMessage;
    private List<MatchResultInfo> duplicates;
    private long processingTimeMs;
}
