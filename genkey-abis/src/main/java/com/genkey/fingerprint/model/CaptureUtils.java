package com.genkey.fingerprint.model;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.platform.utils.CollectionUtils;

public class CaptureUtils {
	

	private static final String FORMAT_RAW = "RAW";

	public static ImageData asImageData(CaptureResult captureResult) {
		String format  = captureResult.getImageFormat();
		ImageData result;
		if (format.equalsIgnoreCase(FORMAT_RAW)) {
			result = new ImageData(captureResult.getWidth(), captureResult.getHeight(), captureResult.getImageData(), captureResult.getResolution());
		} else {
			result = new ImageData(captureResult.getImageData(), captureResult.getImageFormat());
		}
		return result;
	}
	
	public static List<CaptureResult> segmentCaptureResult(MultipleCaptureResult multiCaptureResult, int [] fingers) {
		
		ImageData imageData = asImageData(multiCaptureResult);	
		Map<Integer,Integer> qualityScores = new HashMap<>();
		Map<Integer, ImageData> segments = segmentImage(imageData, fingers, qualityScores); 
		
		List<CaptureResult> captureSegments = CollectionUtils.newList();
		for(Map.Entry<Integer, ImageData> entry : segments.entrySet()) {
			int fingerId = entry.getKey();
			ImageData segment = entry.getValue();
			int qualityScore = qualityScores.get(fingerId);
			CaptureResult result = asCaptureResult(multiCaptureResult, segment, fingerId, qualityScore, "RAW");
			captureSegments.add(result);
		}
		return captureSegments;
	}
	
	public static CaptureResult asCaptureResult(CaptureResult mc, ImageData segment, int fingerId, int qualityScore, String format) {
		byte [] imageData = segment.getPixelData();
		return ((CaptureResult) mc).toBuilder().finger(fingerId)
					.height(segment.getHeight()).width(segment.getWidth())
					.quality(qualityScore)
					.imageData(imageData)
					.imageFormat(format)
					.build();
		
	}
	
	/**
	 * Performs segmentation 
	 * @param imageData
	 * @param fingers
	 * @param qualityScores 
	 * @return
	 */
	public static Map<Integer, ImageData> segmentImage(ImageData imageData, int[] fingers, Map<Integer,Integer> qualityScores) {
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
			if (qualityScores != null) {
				qualityScores.put(fingerId, imageContext.getQualityInfo(ix).getQualityScore());
			}
		}
		return result;
	}
	
	public static FingerEnrollmentReference asFingerEnrollmentReference(CaptureResult captureResult) {
		return null;
	}

	public static boolean isNullArray(byte [] array) {
		return array == null || array.length == 0;
	}
	
	public static boolean isNullString(String encoding) {
		return encoding == null || encoding.length() == 0;
	}
	
	public static void checkImageData(CaptureResult captureResult) {
		if (isNullArray(captureResult.getImageData()) && ! isNullString(captureResult.getTemplateBase64())) {
			captureResult.setImageData(Base64.getDecoder().decode(captureResult.getTemplateBase64()));
		}
	}
	
	public static FingerprintData asFingerprintData(CaptureResult captureResult) {
		checkImageData(captureResult);
		return FingerprintData.builder()
        .finger(captureResult.getFinger())
        .imageData(captureResult.getImageData())
        .imageFormat(captureResult.getImageFormat())
        .quality(captureResult.getQuality())
        .width(captureResult.getWidth())
        .height(captureResult.getHeight())
        .resolution(captureResult.getResolution())
        .build();		
	}
	
}
