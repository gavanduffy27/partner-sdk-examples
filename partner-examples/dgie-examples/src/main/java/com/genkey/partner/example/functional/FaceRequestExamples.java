package com.genkey.partner.example.functional;

import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
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
		if (! abisService.existsSubject(biographicId)) {
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

	public void identifyExample() {
		identifyExample(EnrollmentSubjectID);
	}
	public void identifyExample(String biographicId) {
		SubjectEnrollmentReference enrollmentRef = faceOnlyEnroll(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		/*
		if (! abisService.existsSubject(biographicId)) {
			return;
		}
		*/
		MatchEngineResponse response  = abisService.querySubject(enrollmentRef, false);
		if (!response.isSuccess()) {
			printMessage("Handling REST response failure on query of " + enrollmentRef.getRequestID() + " with error code "
					+ response.getStatusCode());
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
	
	public SubjectEnrollmentReference  faceOnlyEnroll(String biographicId) {
		SubjectEnrollmentReference subject = new SubjectEnrollmentReference();
		subject.setSubjectID(biographicId);
		EnrollmentUtils.enrollFacePortrait(subject);		
		return subject;
	}
	
}
