package com.genkey.partner.workshop;

import java.util.List;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicAttribute;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicProfileRecord.Gender;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;

/**
 * 
 */
public class ABISUnknownSubjectExample extends BMSWorkshopExample{

	static String TestSubject = "1";
	
	public static void main(String[] args) {
		PartnerExample test = new ABISUnknownSubjectExample();
		test.processCommandLine(args);
	}
	
	protected void runAllExamples() {
		queryFaceExample();
		queryFingerExample();
		queryBothExample();
		insertExample();
	}

	public void queryFaceExample() {
		queryFaceExample(TestSubject);
	}

	public void queryFaceExample(String testSubject) {
		queryFaceExample(testSubject, false, false);
	}
	

	public void queryFaceExample(String biographicID, boolean insertIfNoMatch, boolean withBiographics) {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		EnquireStatus status = abisService.enquireSubject(biographicID);
		if (status.existsSubject()) {
			return;
		}
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();
		enrollRef.setSubjectID(biographicID);
		EnrollmentUtils.enrollFacePortrait(enrollRef, 1);
		
		if (withBiographics) {
			
			// Read the biographics from the unknown subject's document.
			BiographicProfileRecord biographicRecord = EnrollmentUtils.getSimpleBiographicRecord(biographicID, "Shaka", "Dacaptain",Gender.Male);
			
			// Add the string encoded properties of the biographic record as matching context with existing records
			//biographicRecord.exportToProperties(enrollRef.getBiographicData());
			enrollRef.setBiographicData(biographicRecord.getBiographicData());
		}
		
		MatchEngineResponse response = abisService.querySubject(enrollRef);		
		boolean matchesFound;
		if (response.hasMatchResults()) {
			matchesFound = checkAnyTrue(enrollRef, response.getMatchResults());
		} else {
			matchesFound=false;
		}
		if (insertIfNoMatch && ! matchesFound) {
			this.insertTestSubject(biographicID, true);
		}
		
	}
	
	
	private boolean checkAnyTrue(SubjectEnrollmentReference enrollRef, List<MatchResult> matchResults) {
		ImageBlob querySubjectPortrait = enrollRef.getFacePortrait();
		
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		
		boolean isDuplicate=false;
		for (MatchResult result : matchResults) {
			String matchId = result.getSubjectID();
			BiographicProfileRecord biographicRecord = biographicService.fetchBiographicRecord(matchId);
			double faceScore = result.getFaceScore();
			ImageBlob matchFace = biographicRecord.getPortrait();
			if ( super.adjudicateCheck(querySubjectPortrait, matchFace)) {
				isDuplicate=true;
			}
		}
		return isDuplicate;
	}

	public void queryFingerExample() {
		queryFingerExample(TestSubject, false, false);
	}
	
	public void queryFingerExample(String biographicId, boolean vetoMatch, boolean insertIfNoMatch) {
		
		int subjectId = Integer.valueOf(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();		
		// Enroll from first sample
		enrollRef.setTargetFingers(TenFingers);
		int startSample=1;
		int maxPresentations=1;
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectId, startSample, maxPresentations);
		
		MatchEngineResponse response = abisService.querySubject(enrollRef);
		
		boolean matchesFound = response.hasMatchResults();
		if (matchesFound) {
			// typically we expect only 1 result but there can be more
			MatchResult matchResult = response.getMatchResults().get(0);
			String matchid = matchResult.getSubjectID();
			double fingerScore = matchResult.getMatchScore();
			printResult("Match score", fingerScore);
			
			if (vetoMatch) {
				// WE have decided this is not a match .. and so we proceed with insert
				matchesFound=false;
			}			
		} 
		
		if ( ! matchesFound && insertIfNoMatch) {
			// proceed with enrollment
			insertTestSubject(biographicId, true);
		}
		
	}

	public void queryBothExample() {
		queryBothExample(TestSubject, false, true);
	}

	public void queryBothExample(String biographicId, boolean vetoMatch, boolean insertIfNoMatch) {
		int subjectId = Integer.valueOf(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();		
		enrollRef.setTargetFingers(TenFingers);
		int sampleIndex=1;
		int numSamples=1;
		
		// Exactly the same as for finger with face added
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectId, sampleIndex, numSamples);
		EnrollmentUtils.enrollFacePortrait(enrollRef, sampleIndex);
		
		
		MatchEngineResponse response = abisService.querySubject(enrollRef);
		
		boolean matchesFound = response.hasMatchResults();
		if (matchesFound) {
			// typically we expect only 1 result but there can be more
			MatchResult matchResult = response.getMatchResults().get(0);
			String matchid = matchResult.getSubjectID();
			double fingerScore = matchResult.getMatchScore();
			printResult("Match score", fingerScore);
			
			if (vetoMatch) {
				// WE have decided this is not a match .. and so we proceed with insert
				matchesFound=false;
			}
			
		} 
		
		if ( ! matchesFound && insertIfNoMatch) {
			insertTestSubject(biographicId, true);
		}		
	}
	
	public void insertExample() {
		insertTestSubject(TestSubject, true);
	}
	
	public void insertTestSubject(String biographicId, boolean withFace) {
		// Some kind of biographic record
		int subjectId = Integer.valueOf(biographicId);

		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		
		BiographicProfileRecord record = EnrollmentUtils.getBiographicRecord(String.valueOf(subjectId), "John", "Brown");		
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference(biographicId);
		
		
		
		enrollRef.setTargetFingers(TenFingers);

		int sampleIndex=1;
		int numSamples=1;
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectId, sampleIndex, numSamples);
		EnrollmentUtils.enrollFacePortrait(enrollRef, sampleIndex);
		
		biographicService.insertBiographicRecord(record);

		MatchEngineResponse response = abisService.insertSubject(enrollRef);
		
		if (response.hasMatchResults())  {
			// Handle duplicates if detected ..
		}
		
		
	}
	
}




