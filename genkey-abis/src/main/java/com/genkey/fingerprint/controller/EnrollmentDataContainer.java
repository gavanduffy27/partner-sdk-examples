package com.genkey.fingerprint.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.fingerprint.model.BiometricRequest;
import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.FingerprintData;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import com.genkey.fingerprint.util.CaptureUtils;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;

public class EnrollmentDataContainer {
	private static final String FORMAT_RAW = "RAW";

	SubjectEnrollmentReference enrolmentReference;

	BiographicProfileRecord biographicProfileRecord;

	boolean autoExtract;

	public EnrollmentDataContainer() {
		this(true);
	}

	public EnrollmentDataContainer(boolean autoExtract) {
		this.setAutoExtract(autoExtract);
	}

	public EnrollmentDataContainer(String biographicId) {
		this(biographicId, true);
	}

	public EnrollmentDataContainer(String biographicId, boolean autoExtract) {
		this.setSubjectID(biographicId);
		this.setAutoExtract(autoExtract);
	}

	public void addBiometricRequest(BiometricRequest request) {
		EnquireStatus status = CaptureUtils.getEnquireStatus(request);

		if (status.isAfisPresent()) {
			for (FingerprintData fp : request.getFingerprints()) {
				addFingerData(fp);
			}
		}
		if (status.isFacePresent()) {
			this.addFacePortrait(request.getFaceImage(), request.getFaceImageFormat());
		}
	}

	public void addFingerData(FingerprintData fpData) {
		ImageData imageData = CaptureUtils.asImageData(fpData);
		this.addImageData(imageData, fpData.getFinger());
	}

	public void addFacePortrait(byte[] imageEncoding, String format) {
		this.getEnrolmentReference().setFacePortrait(new ImageBlob(imageEncoding, format));
	}

	public void addSingleFingerCapture(CaptureResult result) {
		addImageData(CaptureUtils.asImageData(result), result.getFinger());
	}

	public void addMultipleFingerCapture(MultipleFingerCaptureResult result) {
		ImageData fullImage = CaptureUtils.asImageData(result);
		Map<Integer, ImageData> segmentMap = CaptureUtils.segmentImage(fullImage, result.getFingers());
		for (Map.Entry<Integer, ImageData> entry : segmentMap.entrySet()) {
			this.addImageData(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Upload image files into container
	 * 
	 * @param imageFiles
	 * @param fingers
	 * @param resolution
	 * @throws IOException
	 */
	public void importUploadFingerPrints(List<MultipartFile> imageFiles, int[] fingers, int resolution)
			throws IOException {
		for (int ix = 0; ix < imageFiles.size(); ix++) {
			ImageData imageData = CaptureUtils.asImageData(imageFiles.get(ix), resolution, CaptureUtils.FORMAT_BMP);
			this.addImageData(imageData, fingers[ix]);
		}
	}

	public void importUploadFace(MultipartFile faceFile) throws IOException {
		ImageBlob imageBlob = CaptureUtils.asImageBlob(faceFile, CaptureUtils.FORMAT_JPEG);
		this.getEnrolmentReference().setFacePortrait(imageBlob);
	}

	public void addImageData(ImageData imageData, int fingerId) {
		FingerEnrollmentReference enrollRef = new FingerEnrollmentReference(fingerId, autoExtract);
		enrollRef.addImageData(imageData, ImageData.FORMAT_WSQ);
		this.getEnrolmentReference().add(enrollRef);
	}

	public SubjectEnrollmentReference getEnrolmentReference() {
		if (enrolmentReference == null) {
			this.enrolmentReference = new SubjectEnrollmentReference();
		}
		return enrolmentReference;
	}

	public void setEnrolmentReference(SubjectEnrollmentReference enrolmentReference) {
		this.enrolmentReference = enrolmentReference;
	}

	public BiographicProfileRecord getBiographicProfileRecord() {
		if (biographicProfileRecord == null) {
			biographicProfileRecord = this.biographicService().createProfileRecord(this.getSubjectID());
		}
		return biographicProfileRecord;
	}

	public void setBiographicProfileRecord(BiographicProfileRecord biographicProfileRecord) {
		this.biographicProfileRecord = biographicProfileRecord;
	}

	public boolean isAutoExtract() {
		return autoExtract;
	}

	public void setAutoExtract(boolean autoExtract) {
		this.autoExtract = autoExtract;
	}

	public EnquireStatus enquireStatus() {
		return abisService().enquireSubject(this.getSubjectID());
	}

	public MatchEngineResponse query() {
		return abisService().querySubject(this.getEnrolmentReference());
	}

	public MatchEngineResponse insert() {
		return abisService().insertSubject(this.getEnrolmentReference());
	}

	public MatchEngineResponse update() {
		return abisService().updateSubject(this.getEnrolmentReference());
	}

	public VerifyResponse verify() {
		return abisService().verifySubject(this.getEnrolmentReference());
	}

	GenkeyABISService abisService() {
		return ABISServiceModule.getABISService();
	}

	BiographicService biographicService() {
		return DGIEServiceModule.getBiographicService();
	}

	public void setSubjectID(String subjectId) {
		this.getEnrolmentReference().setSubjectID(subjectId);
	}

	public String getSubjectID() {
		return this.getEnrolmentReference().getSubjectID();
	}

	private void exampleAccessCode(EnrollmentDataContainer appContainer) {
		SubjectEnrollmentReference enrolmentRef = appContainer.getEnrolmentReference();

		enrolmentRef.setTargetFingers(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
		byte[] faceEncoding = new byte[10];
		enrolmentRef.setFacePortrait(faceEncoding, "jpeg");

		int maxPresentations = 2;

		int presentationCount = 0;
		while (!enrolmentRef.isComplete() && presentationCount < maxPresentations) {
			int fingersRequired[] = enrolmentRef.getFingersIncomplete();
			// Next capture actions
			presentationCount++;
		}

		// Operation complete ..
		MatchEngineResponse response = appContainer.query();

	}

}
