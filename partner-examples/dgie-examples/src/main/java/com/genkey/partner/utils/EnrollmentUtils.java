package com.genkey.partner.utils;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.example.PartnerTestSuite;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.DateUtils;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.GKRuntimeException;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.ResourceUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;

public class EnrollmentUtils {

	static org.slf4j.Logger logger = LoggerFactory.getLogger(EnrollmentUtils.class);

	public static String EnrolmentRecordSet = "partner/enrolments";

	public static SubjectEnrollmentReference getLegacyTestSubject() {
		return enrollSubject(1, PartnerExample.Thumbs, 1, 1);
	}

	public static SubjectEnrollmentReference enrollSubject(int subjectId, int[] fingers, int startSample,
			int maxSamples) {
		SubjectEnrollmentReference result = new SubjectEnrollmentReference();
		result.setTargetFingers(fingers);
		result.setSubjectID(String.valueOf(subjectId));
		enrollSubject(result, subjectId, startSample, maxSamples);

		return result;
	}

	public static SubjectEnrollmentReference enrollSubjectThumbsBMP(int subjectID, byte[] thumb1, byte[] thumb2) {
		int resolution = 508;

		SubjectEnrollmentReference enrollmentReference = new SubjectEnrollmentReference();

		int[] thumbs = { 1, 6 };
		enrollmentReference.setTargetFingers(thumbs);

		enrollmentReference.setSubjectID(String.valueOf(subjectID));

		FingerEnrollmentReference fingerRef1 = new FingerEnrollmentReference(1, true);
		fingerRef1.addImage(thumb1, ImageData.FORMAT_BMP, 500);
		enrollmentReference.add(fingerRef1);

		FingerEnrollmentReference fingerRef2 = new FingerEnrollmentReference(6, true);
		fingerRef1.addImage(thumb2, ImageData.FORMAT_BMP, 500);
		enrollmentReference.add(fingerRef1);

		// Check those were accepted
		if (!enrollmentReference.isComplete()) {
			// Interrogate which fingers I need additional capture for
			int[] requiredFinges = enrollmentReference.getFingersIncomplete();
		}

		return enrollmentReference;
	}

	public static SubjectEnrollmentReference enrollSubjectThumbsFlat(int subjectID, byte[] thumb1, byte[] thumb2) {
		int resolution = 508;
		int Width = 258;
		int Height = 336;

		SubjectEnrollmentReference enrollmentReference = new SubjectEnrollmentReference();

		int[] thumbs = { 1, 6 };
		enrollmentReference.setTargetFingers(thumbs);
		enrollmentReference.setSubjectID(String.valueOf(subjectID));

		FingerEnrollmentReference fingerRef1 = new FingerEnrollmentReference(1, true);
		// Construct with raw data
		ImageData imageData = new ImageData(Width, Height, thumb1, resolution);
		fingerRef1.addImageData(imageData, ImageData.FORMAT_WSQ);
		enrollmentReference.add(fingerRef1);

		FingerEnrollmentReference fingerRef2 = new FingerEnrollmentReference(6, true);
		ImageData imageData2 = new ImageData(Width, Height, thumb2, resolution);
		fingerRef1.addImageData(imageData, ImageData.FORMAT_WSQ);
		enrollmentReference.add(fingerRef2);

		// Check those were accepted
		if (!enrollmentReference.isComplete()) {
			// Interrogate which fingers I need additional capture for
			int[] requiredFinges = enrollmentReference.getFingersIncomplete();
		}

		return enrollmentReference;
	}

	/**
	 * Enroll specified subject using specified sample range of images
	 *
	 * @param result
	 * @param subjectId
	 * @param startSample
	 * @param maxSamples
	 */
	public static void enrollSubject(SubjectEnrollmentReference result, int subjectId, int startSample,
			int maxSamples) {

		result.setSubjectID(String.valueOf(subjectId));

		String sId = result.getSubjectID();
		String domainName = result.getDomainName();

		int[] tgtFingers = result.getTargetFingers();

		int[] fingersPresent = result.getFingersComplete();

		int nSamples = 0;
		int sampleIndex = startSample;
		while (!result.isComplete() && nSamples < maxSamples) {
			int[] fingers = result.getFingersIncomplete();
			// List<ImageData> images2 = TestDataManager.loadImages(subjectId, fingers,
			// sampleIndex);
			Map<Integer, ImageData> imageMap = TestDataManager.loadImagesIfPresent(subjectId, fingers, sampleIndex);
			addCaptureData(result, imageMap);
			sampleIndex++;
			nSamples++;
		}
	}

	public static void enrollFacePortrait(SubjectEnrollmentReference subjectReference) {
		String subjectId = subjectReference.getSubjectID();
		String imageFile = TestDataManager.getPortraitImageFile(subjectId);
		try {
			byte[] encoding = FileUtils.byteArrayFromFile(imageFile);
			String format = FileUtils.extension(imageFile);
			ImageBlob blob = new ImageBlob(encoding, format);
			subjectReference.setFacePortrait(blob);
		} catch (Exception e) {
			logger.error("Test failure on image access for {}", imageFile);
		}
	}

	public static void addCaptureData(SubjectEnrollmentReference enrollmentReference, int[] fingers,
			List<ImageData> images) {
		Map<Integer, ImageData> imageMap = asImageMap(fingers, images);
		addCaptureData(enrollmentReference, imageMap);
		/*
		 * for (int ix = 0; ix < fingers.length; ix++) { int finger = fingers[ix];
		 * ImageData image = images.get(ix); FingerEnrollmentReference fingerReference =
		 * new FingerEnrollmentReference(finger, true);
		 * fingerReference.addImageData(image, ImageData.FORMAT_WSQ);
		 * enrollmentReference.add(fingerReference); }
		 */
	}

	private static Map<Integer, ImageData> asImageMap(int[] fingers, List<ImageData> images) {
		Map<Integer, ImageData> result = CollectionUtils.newMap();
		for (int ix = 0; ix < fingers.length; ix++) {
			result.put(fingers[ix], images.get(ix));
		}
		return result;
	}

	public static void addCaptureData(SubjectEnrollmentReference enrollmentReference,
			Map<Integer, ImageData> fingerData) {
		for (Map.Entry<Integer, ImageData> entry : fingerData.entrySet()) {
			int finger = entry.getKey();
			ImageData image = entry.getValue();
			FingerEnrollmentReference fingerReference = new FingerEnrollmentReference(finger, true);
			fingerReference.addImageData(image, ImageData.FORMAT_WSQ);
			enrollmentReference.add(fingerReference);
		}
	}

	// public static void enrollSubject()

	public static BiographicProfileRecord getSimpleBiographicRecord(String biographicId, String firstName,
			String lastName, String gender) {
		BiographicProfileRecord record = PartnerTestSuite.getBiographicService().createProfileRecord(biographicId);

		Date dob = DateUtils.mkDate(27, 1, 1964);

		record.setFirstName(firstName);
		record.setLastName(lastName);
		record.setDateOfBirth(dob);
		record.setGender(gender);
		byte[] resourceData = readResourceBytes("passport.jpg");
		ImageBlob blob = new ImageBlob(resourceData, ImageUtils.EXT_JPEG);
		record.setPortrait(blob);

		return record;
	}

	public static BiographicProfileRecord getBiographicRecord(String biographicId, String firstName, String lastName) {
		BiographicProfileRecord result = PartnerTestSuite.getBiographicService().createProfileRecord(biographicId);
		populateTestRecord(result, firstName, lastName);
		return result;
	}

	public static void populateTestRecord(BiographicProfileRecord record, String firstName, String lastName) {
		record.setFirstName(firstName);
		record.setLastName(lastName);
		// BufferedImage passport = readResourceImage("passport.jpg");
		byte[] resourceData = readResourceBytes("passport.jpg");
		ImageBlob blob = new ImageBlob(resourceData, ImageUtils.EXT_JPEG);
		record.setPortrait(blob);
		record.setAttributeBytes("passport_jpg", resourceData);
	}

	public static byte[] readResourceBytes(String resourceName) {
		byte[] result;
		try {
			result = ResourceUtils.getResourceAsBytes(resourceName);
		} catch (Exception e) {
			throw new GKRuntimeException(e);
		}
		return result;
	}

	public static BufferedImage readResourceImage(String resource) {
		BufferedImage result = null;
		try {
			byte[] data = ResourceUtils.getResourceAsBytes(resource);
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			result = ImageIO.read(bis);
		} catch (Exception e) {
			throw new GKRuntimeException(e);
		}
		return result;
	}

	public static List<SubjectEnrollmentReference> accessEnrollmentRecords(long subjectId, String domain) {
		List<Integer> samples = TestDataManager.getSubjectSamples(subjectId);
		List<SubjectEnrollmentReference> result = new ArrayList<>();
		for (int sample : samples) {
			SubjectEnrollmentReference enrolRef = accessEnrollmentRecord(subjectId, sample, domain);
			result.add(enrolRef);
		}
		return result;
	}

	/**
	 * Utility script to generate preprocessed EnrollmentRecords from a image
	 * fileset
	 *
	 * @param imageSourcePath
	 * @param enrollmentPath
	 * @param domain
	 */
	public static void processEnrollmentRecords(String imageSourcePath, String enrollmentPath, String domain) {
		TestDataManager.setImageDirectory(imageSourcePath);
		TestDataManager.setUseSubjectFolders(true);
		TestDataManager.setImageFormat(ImageData.FORMAT_WSQ);
		TestDataManager.setEnrollmentPath(enrollmentPath);
		String sourcePath = FileUtils.expandConfigPath(imageSourcePath);
		String[] subjectImagePaths = FileUtils.getSubDirectories(imageSourcePath, true);
		for (String subjectImagePath : subjectImagePaths) {
			createEnrollmentRecords(subjectImagePath, enrollmentPath, domain);
		}
	}

	public static void createEnrollmentRecords(String subjectImagePath, String enrollmentPath, String domain) {
		String subjectName = FileUtils.baseName(subjectImagePath);
		long subjectId = Long.valueOf(subjectName);
		String[] subjectSamplePaths = FileUtils.getSubDirectories(subjectImagePath);
		for (String samplePath : subjectSamplePaths) {
			int sampleIndex = Integer.valueOf(samplePath);
			accessEnrollmentRecord(subjectId, sampleIndex, domain);
		}
	}

	public static int shiftMod(int finger, int shift, int modulo) {
		while (shift < 0) {
			shift += modulo;
		}
		int result = (finger + shift - 1) % modulo + 1;
		return result;
	}

	/**
	 * Mutates a record with a cyclic shift to simulate creation of a different
	 * record.
	 *
	 * @param enrolRef
	 * @param shift
	 * @return
	 */
	public static SubjectEnrollmentReference shuffleEnrollmentRecord(SubjectEnrollmentReference enrolRef, int shift) {
		int[] fingers = enrolRef.getFingersPresent();
		SubjectEnrollmentReference result = new SubjectEnrollmentReference();
		result.setSubjectID(enrolRef.getSubjectID());
		for (int finger : fingers) {
			FingerEnrollmentReference fingerRef = enrolRef.getReferenceForFingerSample(finger, 0);
			FingerEnrollmentReference modFingerRef = new FingerEnrollmentReference(fingerRef.getEncoding());
			int finger2 = shiftMod(finger, shift, 10);
			modFingerRef.setFingerID(finger2);
			result.add(modFingerRef);
		}
		return result;
	}

	public static SubjectEnrollmentReference accessEnrollmentRecord(long subjectId, int sampleIndex, String domain,
			int shift) {
		SubjectEnrollmentReference reference = TestDataManager.getEnrollmentRecord(subjectId, sampleIndex);
		PartnerExample.reassignDomainName(reference, domain);
		if (shift != 0) {
			reference = shuffleEnrollmentRecord(reference, shift);
		}
		return reference;
	}

	public static SubjectEnrollmentReference accessEnrollmentRecord(long subjectId, int sampleIndex, String domain) {
		SubjectEnrollmentReference reference = TestDataManager.getEnrollmentRecord(subjectId, sampleIndex);
		if (reference == null) {
			reference = createEnrollmentRecord(subjectId, sampleIndex, domain);
			TestDataManager.saveEnrollmentRecord(subjectId, sampleIndex, reference);
		} else {
			PartnerExample.reassignDomainName(reference, domain);
		}
		return reference;
	}

	public static SubjectEnrollmentReference restrictFingers(SubjectEnrollmentReference enrolRef, int[] fingers) {
		@SuppressWarnings("unchecked")
		SubjectEnrollmentReference result = new SubjectEnrollmentReference();
		result.setSubjectID(enrolRef.getSubjectID());
		for (int finger : fingers) {
			FingerEnrollmentReference fingerRef = enrolRef.getReferenceForFingerSample(finger, 0);
			result.add(fingerRef);
		}
		int[] fingers2 = result.getFingersPresent();
		return result;
	}

	public static SubjectEnrollmentReference createEnrollmentRecord(long subjectId, int sampleIndex, String domain) {
		List<Integer> fingers = TestDataManager.getSubjectFingers(subjectId, sampleIndex);
		SubjectEnrollmentReference result = new SubjectEnrollmentReference();
		result.setSubjectID(String.valueOf(subjectId));
		result.setDomainName(domain);

		for (int finger : fingers) {
			ImageData image = TestDataManager.loadImage(subjectId, finger, sampleIndex);
			FingerEnrollmentReference fingerRef = new FingerEnrollmentReference();
			fingerRef.setFingerID(finger);
			fingerRef.setAutoExtract(true);
			fingerRef.addImageData(image, ImageData.FORMAT_WSQ);
			result.add(fingerRef);
		}

		return result;
	}
}
