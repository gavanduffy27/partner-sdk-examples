package com.genkey.fingerprint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultInfo {
    
    private String subjectId;
    private double matchScore;
    private int rank;
    private String firstName;
    private String lastName;
}
