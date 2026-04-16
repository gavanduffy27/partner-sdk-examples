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
public class IdentifyResponse {
    
    private boolean found;
    private int candidateCount;
    private List<MatchResultInfo> candidates;
    private int statusCode;
    private String statusMessage;
    private long processingTimeMs;
}
