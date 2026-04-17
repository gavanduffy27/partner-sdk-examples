package com.genkey.fingerprint.util;

import java.io.IOException;
import java.util.ArrayList;
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
	public static final String FORMAT_JPEG = "JPG";

	public static boolean imageRotation;

	public static boolean isImageRotation() {
		return imageRotation;
	}

	public static void setImageRotation(boolean imageRotation) {
		CaptureUtils.imageRotation = imageRotation;
	}

	public static ImageData asImageData(CaptureResult captureResult) {
		String format = captureResult.getImageFormat();
		ImageData result;
		if (format.equalsIgnoreCase(FORMAT_RAW)) {
			result = new ImageData(captureResult.getWidth(), captureResult.getHeight(), captureResult.getImageData(),
					captureResult.getResolution());
		} else {
			result = new ImageData(captureResult.getImageData(), captureResult.getImageFormat());
		}
		return result;
	}

	public static ImageData asImageData(FingerprintData fpData) {
		ImageData result;
		if (fpData.getImageFormat().equals(CaptureUtils.FORMAT_RAW)) {
			result = new ImageData(fpData.getWidth(), fpData.getHeight(), fpData.getImageData(),
					fpData.getResolution());
		} else {
			result = new ImageData(fpData.getImageData(), fpData.getImageFormat());
		}
		return result;
	}

	public static ImageData asImageData(MultipartFile file, int resolution, String defaultFormat) throws IOException {
		String filename = file.getOriginalFilename();
		String format = filename != null && filename.contains(".")
				? filename.substring(filename.lastIndexOf(".") + 1).toUpperCase()
				: defaultFormat;
		byte[] imageData = file.getBytes();
		return new ImageData(imageData, format, resolution);
	}

	public static ImageBlob asImageBlob(MultipartFile file, String defaultFormat) throws IOException {
		String filename = file.getOriginalFilename();
		String format = filename != null && filename.contains(".")
				? filename.substring(filename.lastIndexOf(".") + 1).toUpperCase()
				: defaultFormat;
		byte[] imageData = file.getBytes();
		return new ImageBlob(imageData, format);
	}

	public static List<CaptureResult> segmentCaptureResult(MultipleFingerCaptureResult multiCaptureResult,
			int[] fingers) {

		ImageData imageData = asImageData(multiCaptureResult);
		Map<Integer, ImageData> segments = segmentImage(imageData, fingers);

		List<CaptureResult> captureSegments = CollectionUtils.newList();
		for (Map.Entry<Integer, ImageData> entry : segments.entrySet()) {
			int fingerId = entry.getKey();
			ImageData segment = entry.getValue();
			CaptureResult result = asCaptureResult(multiCaptureResult, segment, fingerId, FORMAT_RAW);
			captureSegments.add(result);
		}
		return captureSegments;
	}

	public static CaptureResult asCaptureResult(CaptureResult mc, ImageData segment, int fingerId, String format) {
		byte[] imageData = segment.getPixelData();
		return ((CaptureResult) mc).toBuilder().finger(fingerId).height(segment.getHeight()).width(segment.getWidth())
				.imageData(imageData).imageFormat(format).build();

	}

	/**
	 * Performs segmentation
	 * 
	 * @param imageData
	 * @param fingers
	 * @param qualityScores
	 * @return
	 */
	public static Map<Integer, ImageData> segmentImage(ImageData imageData, int[] fingers) {
		if (CaptureUtils.isImageRotation()) {
			imageData = ImageUtils.rotateImageData(imageData, Math.PI);
		}
		ImageContext imageContext = new ImageContext(imageData, fingers);
		if (imageContext.isBlocked()) {
			return null;
		}
		Map<Integer, ImageData> result = new HashMap<>();
		int fingerIds[] = imageContext.getFingers();
		for (int ix = 0; ix < imageContext.count(); ix++) {
			int fingerId = fingerIds[ix];
			ImageData segment = imageContext.extractImageSegment(ix);
			result.put(fingerId, segment);
		}
		return result;
	}

	public static void rotateImageData(ImageData imageData, double pi) {
		//ImageProcessUtils.
	}

	public static FingerEnrollmentReference asFingerEnrollmentReference(CaptureResult captureResult) {
		return null;
	}

	public static boolean isNullRequest(BiometricRequest request) {
		return isNullArray(request.getFaceImage()) || isNullContainer(request.getFingerprints());
	}

	public static boolean isNullContainer(Collection<?> container) {
		return container == null || container.size() == 0;
	}

	public static boolean isNullArray(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullArray(int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullString(String encoding) {
		return encoding == null || encoding.length() == 0;
	}

	public static void checkImageData(CaptureResult captureResult) {
		if (isNullArray(captureResult.getImageData()) && !isNullString(captureResult.getTemplateBase64())) {
			captureResult.setImageData(Base64.getDecoder().decode(captureResult.getTemplateBase64()));
		}
	}

	public static FingerprintData asFingerprintData(CaptureResult captureResult) {
		checkImageData(captureResult);
		return FingerprintData.builder().finger(captureResult.getFinger()).imageData(captureResult.getImageData())
				.imageFormat(captureResult.getImageFormat()).quality(captureResult.getQuality())
				.width(captureResult.getWidth()).height(captureResult.getHeight())
				.resolution(captureResult.getResolution()).build();
	}

	public static List<FingerprintData> asFingerprints(List<MultipartFile> files, int[] fingers, String defaultFormat)
			throws IOException {
		List<FingerprintData> result = new ArrayList<>();
		for (int ix = 0; ix < files.size(); ix++) {
			FingerprintData fingerprint = asFingerprintData(files.get(ix), fingers[ix], defaultFormat);
			result.add(fingerprint);
		}
		return result;
	}

	public static FingerprintData asFingerprintData(MultipartFile file, int fingerId, String defaultFormat)
			throws IOException {
		String filename = file.getOriginalFilename();
		String format = filename != null && filename.contains(".")
				? filename.substring(filename.lastIndexOf(".") + 1).toUpperCase()
				: defaultFormat;

		return FingerprintData.builder().finger(fingerId).imageData(file.getBytes()).imageFormat(format).build();
	}

	public static boolean importFingerprintUpload(BiometricRequest request, List<MultipartFile> images, int[] fingers)
			throws IOException {
		if (isNullContainer(images) && isNullArray(fingers)) {
			return true;
		}
		if (images.size() != fingers.length) {
			return false;
		}
		List<FingerprintData> fingerprints = CaptureUtils.asFingerprints(images, fingers, FORMAT_BMP);
		request.setFingerprints(fingerprints);
		return true;
	}

	public static void importFaceUpload(BiometricRequest request, MultipartFile face) throws IOException {

	}

}
