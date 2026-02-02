package com.genkey.partner.example.functional;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.service.params.BaseParameter;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.service.params.ParameterStatus;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;

public class FaceRequestExamples extends FunctionalTestExample {

	public static int EnrollmentSubject = 1;
	public static String EnrollmentSubjectID = String.valueOf(EnrollmentSubject);

	public static void main(String[] args) {
		PartnerExample test = new FaceRequestExamples();
		test.processCommandLine(args);
	}

	public void setUp() {
		super.setUp();
		String abisConnection = getTestABISService().waitABISConnection(20000, 2000);
		printObject("Connection Details", abisConnection);
		TestABISService abisService = this.getTestABISService();
		abisService.setApplyDomain(true);
	}

	@Override
	protected void runAllExamples() {
		verifyExample();
		identifyExample();
	}

	public void verifyExample() {
		verifyExample(EnrollmentSubjectID);
	}

	public void verifyExample(String biographicId) {
		SubjectEnrollmentReference enrollmentRef = faceOnlyEnroll(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		if (!abisService.existsSubject(biographicId)) {
			return;
		}
		VerifyResponse verifyResponse = abisService.verifySubject(enrollmentRef);
		if (!verifyResponse.isSuccess()) {
			handleRESTFailure(enrollmentRef, verifyResponse, abisService);
		} else if (!verifyResponse.isVerified()) {
			printMessage("Verification failure with score " + verifyResponse.getMatchResult().getMatchScore());
			handleVerificationFailure(enrollmentRef, verifyResponse);
		} else {
			printMessage("Verification success with score " + verifyResponse.getMatchResult().getMatchScore());
			displayMatchResult(verifyResponse.getMatchResult());
		}
	}

	public void updateExample() {
		faceUpdateExample(EnrollmentSubjectID);
	}

	
	public void faceEnrollExample() {
		faceEnrollExample(EnrollmentSubjectID);
	}
 	
	/**
	 * Performs face only enrolment including biographicID but excluding the fingerprints.
	 * 
	 * @param biographicId
	 */
	public void faceEnrollExample(String biographicId) {
		SubjectEnrollmentReference enrollmentRef = faceOnlyEnroll(biographicId);
		//GenkeyABISService abisService = ABISServiceModule.getABISService();
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = BiographicServiceModule.getBiographicService();

		EnquireStatus status = abisService.enquireSubject(biographicId);
		if (status.isFacePresent() ) {
			
			VerifyResponse verifyResponse = abisService.verifySubject(enrollmentRef);
			boolean verifyStatus = verifyResponse.isVerified();
			printResult("Verify status", verifyStatus);
			displayMatchResult(verifyResponse.getMatchResult());
			return;
		}
		
		BiographicProfileRecord biographicRecord; 
		// Capture the biographic record 
		if (! status.isBiographicPresent()) {
			biographicRecord = EnrollmentUtils.getBiographicRecord(biographicId, "john", "doe", false);
			boolean flgEnroll = biographicService.insertBiographicRecord(biographicRecord);
			if (!flgEnroll) {
				handleBiographicInsertFailure(biographicRecord);				
			}			
		} else {
			biographicRecord =  biographicService.fetchBiographicRecord(biographicId);
		}
		
		MatchEngineResponse response = abisService.insertSubject(enrollmentRef);
		if (response.isSuccess()) {
			//displayMatchResults(response.getMatchResults());			
			if (response.hasMatchResults()) {
				handleMatchResults(biographicRecord, response);
			}			
		}
	}
	
	public void faceTestFaceExtensions() {
		faceTestFaceExtensions(EnrollmentSubjectID);
	}
	
	public void faceTestFaceExtensions(String biographicId) {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		EnquireStatus status = abisService.enquireSubject(biographicId);
		if (!status.isFacePresent()) {
			this.faceEnrollExample(biographicId);
			status = abisService.enquireSubject(biographicId);
			if (! status.isFacePresent()) {
				handleException(new RuntimeException("Unexpected failure"));
				return;
			}
		}
		
		ImageBlob image = abisService.getFacePortrait(biographicId);
		double quality = abisService.getFaceImageQuality(image);
		printResult("Image quality", quality);
		ImageBlob image2 = EnrollmentUtils.getSubjectPortrait(biographicId,2);
		
		double score = abisService.matchFaces(image, image2);
		if (score < 0) {
			ParameterStatus parameterStatus = abisService.getParameterStatus();
			super.displayParameterStatus(parameterStatus);
		} else {
			printResult("Score", score);			
		}
	}
	
	public void transferEnrolmentTest() {
		
	}

	public void faceUpdateExample(String biographicId) {
		SubjectEnrollmentReference enrollmentRef = faceOnlyEnroll(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		if (!abisService.existsSubject(biographicId)) {
			return;
		}
		UpdateResponse updateResponse = abisService.updateSubject(enrollmentRef);
		if (!updateResponse.isSuccess()) {
			handleRESTFailure(enrollmentRef, updateResponse, abisService);
		} else {
			printMessage("Update success with " + updateResponse.getOperationResult());
		}
	}

	public void identifyExample() {
		identifyExample(EnrollmentSubjectID);
	}

	public void identifyExample(String biographicId) {
		SubjectEnrollmentReference enrollmentRef = faceOnlyEnroll(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		/*
		 * if (! abisService.existsSubject(biographicId)) { return; }
		 */
		MatchEngineResponse response = abisService.querySubject(enrollmentRef, false);
		if (!response.isSuccess()) {
			printMessage("Handling REST response failure on query of " + enrollmentRef.getRequestID()
					+ " with error code " + response.getStatusCode());
			handleRESTFailure(enrollmentRef, response, abisService);
			return;
		} else {
			printResult("MatchEngineResponse", response.getOperationResult());
		}

		BiographicProfileRecord biographicRecord = EnrollmentUtils.getBiographicRecord(biographicId, "john", "doe");

		// Check for matches found and display
		if (response.hasMatchResults()) {
			handleMatchResults(biographicRecord, response);
		}
	}

	@Override
	void doVerifyExample(String biographicId, int[] targetFingers) {
	}

	public void faceQualityCheckExample() {
		faceQualityCheckExample(EnrollmentSubjectID);
	}

	public void faceQualityCheckExample(String biographicId) {
		SubjectEnrollmentReference subject = new SubjectEnrollmentReference();
		subject.setSubjectID(biographicId);
		EnrollmentUtils.enrollFacePortrait(subject);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		ImageBlob blob = subject.getFacePortrait();
		double score = abisService.getFaceImageQuality(blob);
		printResult("Quality Score", score);
	}

	public SubjectEnrollmentReference faceOnlyEnroll(String biographicId) {
		SubjectEnrollmentReference subject = new SubjectEnrollmentReference();
		subject.setSubjectID(biographicId);
		EnrollmentUtils.enrollFacePortrait(subject);
		return subject;
	}
}
