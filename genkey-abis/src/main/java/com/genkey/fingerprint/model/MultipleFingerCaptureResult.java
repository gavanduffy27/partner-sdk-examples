package com.genkey.fingerprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * We may want to extend this to include an array or collection of quality scores if
 * that information is provided by the scanner.
 * 
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class MultipleFingerCaptureResult extends CaptureResult{

	private int [] fingers;

	public MultipleFingerCaptureResult() {
		
	}
	
	public MultipleFingerCaptureResult(CaptureResult captureResult, int [] fingers) {
		CaptureResult.copyTo(captureResult, this);
		this.setFinger(0);
		this.setFingers(fingers);
		
	}
	
	public static MultipleFingerCaptureResultBuilder multiBuilder() {
		return new MultipleFingerCaptureResultBuilder();
	}
	
	
	public static class MultipleFingerCaptureResultBuilder {
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
		private int[] fingers;
		
		public MultipleFingerCaptureResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder statusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder statusMessage(String statusMessage) {
			this.statusMessage = statusMessage;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder finger(int finger) {
			this.finger = finger;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder imageData(byte[] imageData) {
			this.imageData = imageData;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder imageFormat(String imageFormat) {
			this.imageFormat = imageFormat;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder quality(int quality) {
			this.quality = quality;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder width(int width) {
			this.width = width;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder height(int height) {
			this.height = height;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder resolution(int resolution) {
			this.resolution = resolution;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder templateBase64(String templateBase64) {
			this.templateBase64 = templateBase64;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder captureTimeMs(long captureTimeMs) {
			this.captureTimeMs = captureTimeMs;
			return this;
		}
		
		public MultipleFingerCaptureResultBuilder fingers(int[] fingers) {
			this.fingers = fingers;
			return this;
		}
				
		public MultipleFingerCaptureResult build() {
			MultipleFingerCaptureResult result = new MultipleFingerCaptureResult();
			result.setSuccess(success);
			result.setStatusCode(statusCode);
			result.setStatusMessage(statusMessage);
			result.setFinger(finger);
			result.setImageData(imageData);
			result.setImageFormat(imageFormat);
			result.setQuality(quality);
			result.setWidth(width);
			result.setHeight(height);
			result.setResolution(resolution);
			result.setTemplateBase64(templateBase64);
			result.setCaptureTimeMs(captureTimeMs);
			result.setFingers(fingers);
			return result;
		}
	}
	
}
