package com.genkey.fingerprint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureResult {
    
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private int finger;
    private byte[] imageData;
    private String imageFormat;
    private int quality;
    private int width;
    private int height;
    private int resolution;
    private String templateBase64;
    private long captureTimeMs;
}
