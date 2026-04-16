package com.genkey.fingerprint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintData {
    
    private int finger;          // 1-10 (1=right thumb, 2=right index, etc.)
    private byte[] imageData;    // Raw image bytes or encoded image (BMP/WSQ)
    private String imageFormat;  // BMP, WSQ, PNG, JPEG
    private int quality;         // 0-100
    private int width;           // Image width in pixels
    private int height;          // Image height in pixels
    private int resolution;      // DPI (e.g., 500)
    private String templateBase64; // Extracted template if available
}
