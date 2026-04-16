package com.genkey.fingerprint.model;

import java.util.List;

public interface BiometricRequest {
	byte [] getFaceImage();
	String getFaceImageFormat();
	List<FingerprintData> getFingerprints();
}
