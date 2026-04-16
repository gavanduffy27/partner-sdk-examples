package com.genkey.fingerprint.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;

/**
 * Utilities for consuming application CaptureResult or FingerPrintData directly into a SubjectEnrolmentReference
 * object
 */
public class EnrolmentUtils {

	static boolean autoExtract=true;
	
	public static boolean isAutoExtract() {
		return autoExtract;
	}

	public static void setAutoExtract(boolean autoExtract) {
		EnrolmentUtils.autoExtract = autoExtract;
	}

	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, CaptureResult result) {
		addCaptureData(enrolmentRef, result, isAutoExtract());
	}
	
	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, CaptureResult result, boolean autoExtract) {
		ImageData imageData = CaptureUtils.asImageData(result);
		addCaptureData(enrolmentRef, imageData, result.getFinger(), autoExtract);
	}
	
	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, ImageData imageData, int fingerId, boolean autoExtract) {
		FingerEnrollmentReference enrollRef = new FingerEnrollmentReference(fingerId, autoExtract);
		enrollRef.addImageData(imageData, ImageData.FORMAT_WSQ);		
	}

	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, List<CaptureResult> results, boolean autoExtract) {
		for(CaptureResult result :results) {
			addCaptureData(enrolmentRef, CaptureUtils.asImageData(result), result.getFinger(), autoExtract);
		}
	}

	/**
	 * Adds a multiple capture direct to SubjectEnrolmentReference
	 * @param enrolmentRef
	 * @param result
	 * @param autoExtract
	 */
	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, MultipleCaptureResult result, boolean autoExtract) {
		ImageData imageData = CaptureUtils.asImageData(result);
		Map<Integer, ImageData> imageMap = segmentImage(imageData, result.getFingers());
		for(Map.Entry<Integer, ImageData> entry : imageMap.entrySet()) {
			addCaptureData(enrolmentRef, entry.getValue(), entry.getKey(), autoExtract);
		}
	}
	
	public static void addCaptureData(SubjectEnrollmentReference enrolmentRef, FingerprintData fingerprintData, boolean autoExtract) {
		addCaptureData(enrolmentRef, FingerprintDataUtils.asImageData(fingerprintData), fingerprintData.getFinger(), autoExtract);
	}
	
	/**
	 * Performs segmentation 
	 * @param imageData
	 * @param fingers
	 * @param qualityScores 
	 * @return
	 */
	public static Map<Integer, ImageData> segmentImage(ImageData imageData, int[] fingers) {
		ImageContext imageContext = new ImageContext(imageData, fingers);
		if (imageContext.isBlocked()) {
			return null;
		}
		Map<Integer, ImageData> result = new HashMap<>();
		int fingerIds[] = imageContext.getFingers();
		for(int ix=0; ix < imageContext.count(); ix++) {
			int fingerId=fingerIds[ix];
			ImageData segment = imageContext.extractImageSegment(ix);
			result.put(fingerId, segment);
		}
		return result;
	}
	
	
	

}
