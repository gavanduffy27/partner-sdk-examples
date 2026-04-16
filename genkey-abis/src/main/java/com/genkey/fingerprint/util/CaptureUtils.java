package com.genkey.fingerprint.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.fingerprint.model.BiometricRequest;
import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import com.genkey.fingerprint.model.FingerprintData;
import com.genkey.platform.utils.CollectionUtils;

public class CaptureUtils {
	

	public static final String FORMAT_RAW = "RAW";
	public static final String FORMAT_BMP = "BMP";
	public static final String FORMAT_WSQ = "WSQ";
	public static final String FORMAT_JPEG = "JPEG";

	
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
	
	public static ImageData asImageData(FingerprintData fpData) {
		ImageData result;
		if (fpData.getImageFormat().equals(CaptureUtils.FORMAT_RAW)) {
			result= new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(), fpData.getResolution());
		} else {
			result = new ImageData(fpData.getImageData(), fpData.getImageFormat());
		}
		return result;
	}
	
	public static ImageData asImageData(MultipartFile file, int resolution) throws IOException {
        String filename = file.getOriginalFilename();
        String format = filename != null && filename.contains(".") ? 
                filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : FORMAT_BMP;
        byte [] imageData = file.getBytes();
        return new ImageData(imageData, format, resolution);
	}
	
	public static ImageBlob asImageBlob(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String format = filename != null && filename.contains(".") ? 
                filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : FORMAT_JPEG;
        byte [] imageData = file.getBytes();
        return new ImageBlob(imageData, format);		
	}
	
	public static List<CaptureResult> segmentCaptureResult(MultipleFingerCaptureResult multiCaptureResult, int [] fingers) {
		
		ImageData imageData = asImageData(multiCaptureResult);	
		Map<Integer, ImageData> segments = segmentImage(imageData, fingers); 
		
		List<CaptureResult> captureSegments = CollectionUtils.newList();
		for(Map.Entry<Integer, ImageData> entry : segments.entrySet()) {
			int fingerId = entry.getKey();
			ImageData segment = entry.getValue();
			CaptureResult result = asCaptureResult(multiCaptureResult, segment, fingerId, FORMAT_RAW);
			captureSegments.add(result);
		}
		return captureSegments;
	}
	
	public static CaptureResult asCaptureResult(CaptureResult mc, ImageData segment, int fingerId, String format) {
		byte [] imageData = segment.getPixelData();
		return ((CaptureResult) mc).toBuilder().finger(fingerId)
					.height(segment.getHeight()).width(segment.getWidth())
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
	
	public static FingerEnrollmentReference asFingerEnrollmentReference(CaptureResult captureResult) {
		return null;
	}

	public static boolean isNullRequest(BiometricRequest request) {
		return isNullArray(request.getFaceImage()) || isNullContainer(request.getFingerprints());
	}
	
	public static boolean isNullContainer(Collection<?> container) {
		return container == null || container.size()==0;
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
