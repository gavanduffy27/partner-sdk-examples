package com.genkey.partner.example.functional;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.MatchResult.MatchResultDetail;
import com.genkey.abisclient.service.ABISResponse;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicIdentifier;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;

public abstract class FunctionalTestExample  extends PartnerExample {

	public void standardEnrollExample(String biographicId, int [] targetFingers) {
		standardEnrollExample(biographicId, targetFingers,PartnerExample.isFaceMatchEnabled());
	}
	
	
	
	public void standardEnrollExample(String biographicId, int[] targetFingers, boolean faceMatchEnabled) {

		int subjectNumber = Integer.valueOf(biographicId);
		// Access the services - note there are different ways to access the service
		LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
		BiographicService biographicService = BiographicServiceModule.getBiographicService();
		GenkeyABISService abisService = ABISServiceModule.getABISService();

		String domainName = abisService.getDomainName();
		String externalId = BiographicIdentifier.resolveExternalID(biographicId, domainName);

		String domain1 = legacyService.getDomainName();
		String domain2 = biographicService.getDomainName();
		String domain3 = abisService.getDomainName();

		while (!abisService.testAvailable()) {
			printMessage("\nError status " + abisService.getStatusCode() + ":" + abisService.getLastErrorMessage());
			Commons.waitMillis(2000);
		}

		// Check ABIS system is also available
		String abisConnection = abisService.testABISConnection();

		printObject("ABIS Connection", abisConnection);

		SubjectEnrollmentReference subject = new SubjectEnrollmentReference();

		boolean existsSubject = abisService.existsSubject(biographicId);

		if (existsSubject) {
			println("Subject " + externalId + " already exists. Processing as a verification test");
			doVerifyExample(biographicId, targetFingers);
			return;
		}

		// Check and verify against legacy service if present
		if (legacyService.existsSubject(biographicId)) {

			int[] legacyFingers = legacyService.existsSubjectRecord(biographicId);
			subject.setTargetFingers(legacyFingers);
			EnrollmentUtils.enrollSubject(subject, subjectNumber, 1, 1);

			VerifyResponse verifyResponse = legacyService.verifySubject(subject);

			if (!verifyResponse.isVerified()) {
				double matchScore = verifyResponse.getMatchResult().getMatchScore();
				super.printMessage("Warning subject " + biographicId + " failed verification with score " + matchScore);
			}
		} else {
			printMessage("Subject " + biographicId + " not available as legacy subject");

			if (!PartnerExample.UseRemote) {

				subject.setTargetFingers(Thumbs);
				EnrollmentUtils.enrollSubject(subject, subjectNumber, 1, 1);

//				forceTestDomain(subject);
				String subjectId = subject.getSubjectID();
				printMessage("Performing enroll of " + subjectId + " as legacy subject");
				boolean legacyEnroll = legacyService.registerSubject(subject);
				if (!legacyService.isSuccess()) {
					handleRESTFailure(subject, legacyService);
				}
				if (!legacyEnroll) {
					printMessage("Legacy enrollment failed");
				}
			}
		}

		
		// complete a 10 finger enrolment
		subject.setTargetFingers(targetFingers);
		EnrollmentUtils.enrollSubject(subject, subjectNumber, 1, 2);

		if (faceMatchEnabled) {
			EnrollmentUtils.enrollFacePortrait(subject);
		}
		
		// Capture the biographic record and insert it
		BiographicProfileRecord biographicRecord = EnrollmentUtils.getBiographicRecord(biographicId, "john", "doe");
		

		// Access the abis service and insert it
		MatchEngineResponse response = abisService.insertSubject(subject, false);

		boolean handleRestFailure = true;
		if (!response.isSuccess() && handleRestFailure) {
			printMessage("Handling REST response failure on insert of " + biographicId + " with error code "
					+ response.getStatusCode());
			handleRESTFailure(subject, response, abisService);
			return;
		} else {
			printMessage("Insert completed successfully with result " + response.getOperationResult());
		}

		// Capture the biographic record and insert it
		//BiographicProfileRecord biographicRecord = EnrollmentUtils.getBiographicRecord(biographicId, "john", "doe");
		boolean status = biographicService.insertBiographicRecord(biographicRecord);

		if (!status) {
			handleBiographicInsertFailure(biographicRecord);
		}

		// Check for matches found
		if (response.hasMatchResults()) {
			handleMatchResults(biographicRecord, response);
		}

	}
	
	abstract  void doVerifyExample(String biographicId, int[] targetFingers);

	protected static void handleRESTFailure(SubjectEnrollmentReference subject, ABISResponse response,
			RemoteAccessService service) {
		handleRESTFailure(subject, service);
	}

	protected static void handleRESTFailure(SubjectEnrollmentReference subject, RemoteAccessService service) {
		String errorMessage = service.getLastErrorMessage();
		int statusCode = service.getStatusCode();
		String hostName = service.getHostName();
		int port = service.getPort();

		// Display as appropriate
		String message = String.format("%d : %s", statusCode, errorMessage);
		FormatUtils.printBanner(message);
	}
	
	public static void handleBiographicInsertFailure(BiographicProfileRecord biographicRecord) {
		byte[] encoding = Commons.serializeObject(biographicRecord);
		String id = biographicRecord.getBiographicID();
		String pendingRecordFile = FileUtils.expandConfigFile("test/errors/pendingRecords", id, FileUtils.EXT_DAT);
		try {
			FileUtils.byteArrayToFile(encoding, pendingRecordFile);
		} catch (Exception e) {

		}
	}

	protected void handleVerificationFailure(SubjectEnrollmentReference enrollmentRef, VerifyResponse verifyResponse) {
		MatchResult matchResult = verifyResponse.getMatchResult();
		double score = matchResult.getMatchScore();
		// etc
	}

	protected void handleMatchResults(BiographicProfileRecord biographicRecord, MatchEngineResponse response) {
		BiographicService biographicService = this.getBiographicService();
		ImageBlob subjectPortrait = biographicRecord.getPortrait();
		String subjectManifest = getBiographicManifest(biographicRecord);
		for (MatchResult result : response.getMatchResults()) {
			String subjectId = result.getSubjectID();
			BiographicProfileRecord matchRecord = biographicService.fetchBiographicRecord(subjectId);

			if (matchRecord.isNull()) {
				printMessage("Warning no biographic profile record exists for matched record " + subjectId);
				continue;
			}

			double matchScore = result.getMatchScore();
			printResult("Match score", matchScore);
			String matchManifest = getBiographicManifest(matchRecord);
			ImageBlob matchPortrait = matchRecord.getPortrait();
			examineMatchResult(subjectManifest, matchManifest, subjectPortrait, matchPortrait);
			displayMatchResult(result);
		}
	}

	protected void displayMatchResult(MatchResult result) {
		println("Matched " + result.getSubjectID() + " with score " + result.getMatchScore() + " based on "
				+ result.getInstanceCount() + " fingers");
		printHeader("Match details");
		for (MatchResultDetail matchDetail : result.getMatchDetails()) {
			println(matchDetail.getIndex1() + " <==>" + matchDetail.getIndex2() + ":" + matchDetail.getScore());
		}

	}

	protected void examineMatchResult(String subjectManifest, String matchManifest, ImageBlob subjectPortrait,
			ImageBlob matchPortrait) {

	}
	
	
	
}
