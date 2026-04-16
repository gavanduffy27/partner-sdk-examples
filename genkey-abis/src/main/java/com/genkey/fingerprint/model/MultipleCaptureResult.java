package com.genkey.fingerprint.model;

import lombok.Data;

/**
 * We may want to extend this to include an array or collection of quality scores if
 * that information is provided by the scanner.
 * 
 */
@Data
public class MultipleCaptureResult extends CaptureResult{

	private int [] fingers;
	
}
