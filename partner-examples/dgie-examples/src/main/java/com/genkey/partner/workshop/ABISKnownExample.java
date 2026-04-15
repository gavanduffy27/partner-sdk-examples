package com.genkey.partner.workshop;

import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;

public class ABISKnownExample extends BMSWorkshopExample{

	static String TestSubject = "1";
	
	
	public static void main(String[] args) {
		PartnerExample test = new ABISKnownExample();
		test.processCommandLine(args);
	}
	
	protected void runAllExamples() {
		verifyFaceExample();
		verifyFingerExample();
	}


	public void verifyFingerExample() {
		boolean status = verifyFingerExample(TestSubject,2);
	}

	public void verifyFaceExample() {
		verifyFaceExample(TestSubject, 2, false);
	}
	
	public void verifyFaceExample(String biographicId, int sampleIndex, boolean updateIfMatch) {
		int subjectNumber = Integer.valueOf(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference(biographicId);		
		EnrollmentUtils.enrollFacePortrait(enrollRef, sampleIndex);
		
		VerifyResponse response = abisService.verifySubject(enrollRef);
		
		if (! response.isVerified()) {
			// Escalate with fingers.
			enrollRef.setTargetFingers(LeftHandFingers);
			EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectNumber, sampleIndex, 1);
			
			// Just repeat verification with additional biometric data
			response = abisService.verifySubject(enrollRef);			
		}
		
		if (!response.isVerified()) {
			enrollRef.setTargetFingers(RightHandFingers);
			EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectNumber, sampleIndex, 1);
			
			// Just repeat verification with additional biometric data
			response = abisService.verifySubject(enrollRef);			
			
		}
		
		if (response.isVerified() && updateIfMatch) {
			UpdateResponse updateResponse = abisService.updateSubject(enrollRef);
			if (updateResponse.hasMatchResults()) {
				//  can happen ., but rarely
				
			}
		} 
		
	}
	

	// Verification starting with fingers and escalating if required
	public boolean verifyFingerExample(String biographicId, int sampleIndex) {
		int subjectNumber = Integer.valueOf(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference(biographicId);		
		enrollRef.setTargetFingers(LeftHandFingers);
		int sampleCount=1;
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectNumber, sampleIndex, sampleCount);
//		EnrollmentUtils.enrollFacePortrait(enrollRef, 1);
		
		VerifyResponse response = abisService.verifySubject(enrollRef);
		if (response.isVerified()) {
			return true;
		}

		// Escalate with face
		EnrollmentUtils.enrollFacePortrait(enrollRef, 1);
		response = abisService.verifySubject(enrollRef);
		if (response.isVerified()) {
			return true;
		}

		// escalate to 10 fingeers
		enrollRef.setTargetFingers(TenFingers);
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectNumber, sampleIndex, sampleCount);
		response = abisService.verifySubject(enrollRef);
		if ( !response.isVerified()) {
			MatchResult result = response.getMatchResult();
			double fingerScore = result.getMatchScore();
			double biographicScore = result.getBiographicScore();
			double faceScore = result.getFaceScore();
			// Further evaluation - and escalated actions ..
		}
		return response.isVerified();
	}
	
	
}
