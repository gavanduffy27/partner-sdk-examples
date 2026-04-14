package com.genkey.partner.workshop;

import java.util.List;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
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
		queryFaceExample(1);
		queryFingerExample();
		queryBothExample();
		insertExample();
	}

	public void queryFaceExample(int i) {
		queryFaceExample(TestSubject, true);
	}
	

	public void queryFaceExample(String biographicID, boolean insertIfNoMatch) {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();
		enrollRef.setSubjectID(biographicID);
		EnrollmentUtils.enrollFacePortrait(enrollRef, 1);
		MatchEngineResponse response = abisService.querySubject(enrollRef);		
		boolean matchesFound;
		if (response.hasMatchResults()) {
			matchesFound = checkAnyTrue(enrollRef, response.getMatchResults());
		} else {
			matchesFound=false;
		}
		if (insertIfNoMatch && ! matchesFound) {
			// proceeed with enrollment
		}
		
	}
	
	
	private boolean checkAnyTrue(SubjectEnrollmentReference enrollRef, List<MatchResult> matchResults) {
		ImageBlob querySubjectPortrait = enrollRef.getFacePortrait();
		
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		
		boolean isDuplicate=false;
		for (MatchResult result : matchResults) {
			String matchId = result.getSubjectID();
			BiographicProfileRecord biographicRecord = biographicService.fetchBiographicRecord(matchId);
			ImageBlob matchFace = biographicRecord.getPortrait();
			if ( super.adjudicateCheck(querySubjectPortrait, matchFace)) {
				isDuplicate=true;
			}
		}
		return isDuplicate;
	}

	public void queryFingerExample() {
		queryFingerExample(1, false, false);
	}
	
	public void queryFingerExample(int subjectId, boolean vetoMatch, boolean insertIfNoMatch) {
		
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();		
		// Enroll from first sample
		enrollRef.setTargetFingers(TenFingers);
		int startSample=1;
		int numSamples=1;
		EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectId, startSample, numSamples);
		
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
			insertTestSubject(subjectId, true);
		}
		
	}

	public void queryBothExample() {
		queryBothExample(1, false, true);
	}

	public void queryBothExample(int subjectId, boolean vetoMatch, boolean insertIfNoMatch) {
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
			insertTestSubject(subjectId, true);
		}
		
	}
	
	public void insertExample() {
		insertTestSubject(1, true);
	}

	public void insertTestSubject(int subjectId, boolean withFace) {
		// Some kind of biographic record
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		
		BiographicProfileRecord record = EnrollmentUtils.getBiographicRecord(String.valueOf(subjectId), "John", "Brown");		
		
		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();
		enrollRef.setSubjectID(String.valueOf(subjectId));
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
