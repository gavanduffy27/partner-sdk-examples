package com.genkey.fingerprint.model;

import com.genkey.abisclient.ImageData;
import com.genkey.fingerprint.util.CaptureUtils;

public class FingerprintDataUtils {
	
	public static ImageData asImageData(FingerprintData fpData) {
		ImageData result;
		if (fpData.getImageFormat().equals(CaptureUtils.FORMAT_RAW)) {
			result= new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), fpData.getResolution());
		} else {
			result = new ImageData(fpData.getImageData(), fpData.getImageFormat());
		}
		return result;
	}
}
