package com.genkey.fingerprint.model;

import com.genkey.abisclient.ImageData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
public class CaptureResult {
    
    public static CaptureResult copyOf(CaptureResult mc) {
    	return mc.toBuilder().build();
    }
    
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
