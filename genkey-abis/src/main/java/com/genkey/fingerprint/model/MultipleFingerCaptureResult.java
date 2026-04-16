package com.genkey.fingerprint.model;

import lombok.Builder;
import lombok.Data;

/**
 * We may want to extend this to include an array or collection of quality scores if
 * that information is provided by the scanner.
 * 
 */

@Data
public class MultipleFingerCaptureResult extends CaptureResult{

	private int [] fingers;

	public MultipleFingerCaptureResult(CaptureResult captureResult, int [] fingers) {
		CaptureResult.copyTo(captureResult, this);
		this.setFinger(0);
		this.setFingers(fingers);
		
	}
	
}
