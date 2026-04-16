package com.genkey.fingerprint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder=true)
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
    
    public static void copyTo(CaptureResult source, CaptureResult target) {
    	target.success=source.success;
    	target.statusCode=source.statusCode;
    	target.statusMessage=source.statusMessage;
    	target.finger=source.finger;
    	target.imageData=source.imageData;
    	target.imageFormat=source.getImageFormat();
    	target.quality=source.quality;
    	target.width=source.width;
		target.height=source.height;        
    	target.resolution=source.resolution;
    	target.captureTimeMs=source.captureTimeMs;
    }
    
}
