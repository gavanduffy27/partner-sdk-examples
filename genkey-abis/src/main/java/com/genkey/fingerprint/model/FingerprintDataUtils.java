package com.genkey.fingerprint.model;

import com.genkey.abisclient.ImageData;

public class FingerprintDataUtils {
	
	public static ImageData asImageData(FingerprintData fpData) {
		return new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), fpData.getResolution());
	}
}
