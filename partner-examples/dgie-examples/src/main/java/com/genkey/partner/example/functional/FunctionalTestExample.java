package com.genkey.partner.example.functional;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.MatchResult.MatchResultDetail;
import com.genkey.abisclient.service.ABISResponse;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;

public abstract class FunctionalTestExample extends PartnerExample {

	abstract void doVerifyExample(String biographicId, int[] targetFingers);

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

	protected void handleUpdateFailure(SubjectEnrollmentReference enrollmentRef, UpdateResponse updateResponse) {
		// MatchResult matchResult = updateResponse.getMatchResult();
		// double score = matchResult.getMatchScore();
		// etc
		handleRESTFailure(enrollmentRef, updateResponse, getABISService());
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

			Double matchScore = result.getMatchScore();
			printResult("Figngerprint score", matchScore);
			Double faceScore = result.getFaceScore();
			printResult("Face score", faceScore);
			double fusionScore = result.getFusionScore();
			printResult("Fusion score", fusionScore);

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
		if (result.getFaceScore() != null && result.getFaceScore() >= 0) {
			println("Face score =" + result.getFaceScore());
		}
	}

	protected void examineMatchResult(String subjectManifest, String matchManifest, ImageBlob subjectPortrait,
			ImageBlob matchPortrait) {
	}
}
