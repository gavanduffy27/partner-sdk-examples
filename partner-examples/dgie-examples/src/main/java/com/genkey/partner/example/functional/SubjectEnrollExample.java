package com.genkey.partner.example.functional;

import org.junit.Test;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.ext.ImageWrapperSet;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.MatchResult.MatchResultDetail;
import com.genkey.abisclient.service.ABISResponse;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.ImageRequestResponse;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.RestServices;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.afis.jaxb.InspectResponse;
import com.genkey.partner.biographic.BiographicIdentifier;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;

/**
 * A set of examples that show standard functional behaviors
 * 
 * @author Gavan
 *
 */
public class SubjectEnrollExample extends FunctionalTestExample {

	public static int EnrollmentSubject = 1;
	public static String EnrollmentSubjectID = String.valueOf(EnrollmentSubject);

	public static void main(String[] args) {
		PartnerExample test = new SubjectEnrollExample();
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
		enrollExample();
	}

	/**
	 * Test for checking isolation between domains
	 */
	public void testMultipleDomain() {
		RestServices rs = RestServices.getInstance();

		TestABISService service = getTestABISService();

		String currentDomain = service.getDomainName();
		String gkDomain1 = GKDomain + 1;
		String gkDomain2 = GKDomain + 2;

		rs.updateDomainName(currentDomain);

		boolean existsSubject = service.existsSubject(EnrollmentSubjectID);

		// genkeyTest, EnrollmentSDK1 , EnrollmentSDK2

		// enrol subject in first tomains
		// skip
		enrollExample();

		rs.updateDomainName(gkDomain1);
		service.setEnforceDomainFiltering(false);
		//
		enrollExample();
		identifyExample();

		rs.updateDomainName(gkDomain2);
		service.setEnforceDomainFiltering(true);
		enrollExample();
		identifyExample();

		service.deleteDomainSubjects(currentDomain);
		service.deleteDomainSubjects(gkDomain1);
		service.deleteDomainSubjects(gkDomain2);

	}

	public void testExistsSubject() {
		TestABISService service = getTestABISService();
		String currentDomain = service.getDomainName();
		String gkDomain1 = GKDomain + 1;
		String gkDomain2 = GKDomain + 2;

		boolean existsSubject = service.existsSubject(EnrollmentSubjectID);

		service.setDomainName(gkDomain1);
		boolean existsSubject1 = service.existsSubject(EnrollmentSubjectID);

		service.setDomainName(gkDomain2);
		boolean existsSubject2 = service.existsSubject(EnrollmentSubjectID);

	}

	protected void runSampleTests() {

		roundTripExample();

		deleteSubjectExample();
		/*
		 * deleteDomainOnly(); deleteDomainExample();
		 */

		enrollExample();

		verifyExample();
		identifyExample();
		standardDeleteExample();
		enrollExample();
		deleteDomainExample();

		// Real start here

		/*
		 * enrollDuplicateExample(); enrollDuplicateExample(); verifyExample();
		 * identifyExample(); deleteDomainExample(); deleteDomainOnly();
		 * biographicEnrollExample(); //deleteSubjectExample();
		 * //deleteDomainExampleN();
		 */
	}

	public void roundTripTest() {

	}

	/**
	 * Repeat test that shows the creation of subjects, running a test and then
	 * resetting the system. Note this is used only for the purpose of testing the
	 * Test Environment capacity to perform a system reset.
	 * 
	 */
	@Test
	public void deleteDomainExampleN() {
		for (int ix = 0; ix < 3; ix++) {
			deleteDomainExample();
		}
	}

	@Test
	public void commitDeleteTest() {
		TestABISService service = getTestABISService();
		service.commitSubjectDeletes();
	}

	/**
	 * Creates a number of subjects within the current test domain and on completion
	 * wipes all subjects for that domain from the database.
	 * 
	 */
	@Test
	public void deleteDomainExample() {
		String imageDir = TestDataManager.getImageDirectory();

		// Note we access as TestABISService for extended test functions
		TestABISService abisService = getTestABISService();

		int[] fingers = { 1, 6, 3, 4, 7, 8 };
		for (int ix = 0; ix < 5; ix++) {
			int subject = ix + 1;
			String subjectId = String.valueOf(subject);
			boolean flg = abisService.existsSubject(subjectId);
			if (!flg) {
				standardEnrollExample(subjectId, fingers);
			}
			legacyVerify(subjectId);
			doVerifyExample(subjectId, fingers);
			identifyExample(subjectId, fingers);
		}

		// ! Mark them for delete
		println("Deleting all subjects in domain " + abisService.getDomainName());
		abisService.deleteDomainSubjects(abisService.getDomainName());

		println("Commiting delete to ABIS system and invoking restart ");
		// ! Execute the deletes which will perform remote ABIS reset
		abisService.commitSubjectDeletes();

		println("ABIS system reset complete ");
	}

	@Test
	public void deleteDomainOnly() {
		TestABISService abisService = getTestABISService();
		String domainName = abisService.getDomainName();
		println("Deleting all subjects in domain " + abisService.getDomainName());
		abisService.deleteDomainSubjects(abisService.getDomainName());

		println("Commiting delete to ABIS system and invoking restart ");
		// ! Execute the deletes which will perform remote ABIS reset
		abisService.commitSubjectDeletes();

		println("ABIS system reset complete ");

	}

	static String BMSDomain = "BMS";

	@Test
	public void deleteBMSDomain() {
		testDeleteDomain(BMSDomain);
		/*
		 * TestABISService abisService = getTestABISService(); String domainName =
		 * abisService.getDomainName(); abisService.setDomainName(BMSDomain);
		 * println("Deleting all subjects in domain " + abisService.getDomainName());
		 * abisService.deleteDomainSubjects(abisService.getDomainName());
		 * abisService.setDomainName(domainName);
		 * 
		 * println("Commiting delete to ABIS system and invoking restart "); //! Execute
		 * the deletes which will perform remote ABIS reset
		 * abisService.commitSubjectDeletes();
		 * 
		 * println("ABIS system reset complete ");
		 */

	}

	static String GKDomain = "EnrollmentSDK";

	@Test
	public void deleteTestDomain() {
		testDeleteDomain(GKDomain);
	}

	private void testDeleteDomain(String targtDomain) {
		TestABISService abisService = getTestABISService();
		String domainName = abisService.getDomainName();
		abisService.setDomainName(targtDomain);
		println("Deleting all subjects in domain " + abisService.getDomainName());
		abisService.deleteDomainSubjects(abisService.getDomainName());
		abisService.setDomainName(domainName);

		println("Commiting delete to ABIS system and invoking restart ");
		// ! Execute the deletes which will perform remote ABIS reset
		abisService.commitSubjectDeletes();

		println("ABIS system reset complete ");

	}

	/**
	 * Performs a legacy verification on specified subject if it exists
	 * 
	 * @param biographicId
	 */
	public void legacyVerify(String biographicId) {
		LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
		SubjectEnrollmentReference subject = new SubjectEnrollmentReference();
		int subjectNumber = Integer.valueOf(biographicId);
		if (legacyService.existsSubject(biographicId)) {

			int[] legacyFingers = legacyService.existsSubjectRecord(biographicId);
			subject.setTargetFingers(legacyFingers);
			EnrollmentUtils.enrollSubject(subject, subjectNumber, 1, 1);

			VerifyResponse verifyResponse = legacyService.verifySubject(subject);
			double matchScore = verifyResponse.getMatchResult().getMatchScore();

			if (!verifyResponse.isVerified()) {
				super.printMessage("Warning subject " + biographicId + " failed verification with score " + matchScore);
			} else {
				printMessage("Subject " + biographicId + " verified against legacy with score " + matchScore);
			}
		}

	}

	public void standardDeleteExample() {
		TestABISService testService = super.getTestABISService();
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		String biographicId = String.valueOf(EnrollmentSubject);
		boolean existsSubject = abisService.existsSubject(biographicId);
		if (existsSubject) {
			abisService.deleteSubject(biographicId, true);
		}
	}

	public void roundTripExample() {
		standardDeleteExample();
		this.enrollExample();
		this.verifyExample();
		this.identifyExample();
		this.standardDeleteExample();
	}

	/**
	 * Functional test for enroll
	 */
	@Test
	public void enrollExample() {
		standardEnrollExample(EnrollmentSubjectID);
	}

	public void standardEnrollExample(String biographicId) {
		standardEnrollExample(biographicId, TenFingers);
	}


	@Test
	public void biographicEnrollExample() {
		BiographicService service = this.getBiographicService();
		String biographicId = "sergeTest";
		BiographicProfileRecord record1a, record1b;

		if (service.existsBiographicRecord(biographicId)) {
			String externalId = BiographicIdentifier.resolveExternalID(biographicId, service.getDomainName());
			record1a = service.fetchBiographicRecord(biographicId);
			record1b = service.fetchBiographicRecord(externalId);
			printResult("FirstName1", record1a.getFirstName());
			printResult("FirstName2", record1b.getFirstName());

			service.deleteBiographicRecord(biographicId);
		}

		BiographicProfileRecord biographicRecord = EnrollmentUtils.getSimpleBiographicRecord(biographicId, "john",
				"doe", "male");
		boolean status = service.insertBiographicRecord(biographicRecord);

		BiographicProfileRecord record2 = service.fetchBiographicRecord(biographicId);
		String firstName = record2.getFirstName();
		String lastName = record2.getLastName();
		printResult("FirstName", firstName);
		printResult("LastName", lastName);
	}

	private void testServicesAvailable() {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = BiographicServiceModule.getBiographicService();
		if (!abisService.testAvailable()) {
			printError("ABIS service not available");
			return;
		}

		if (abisService.testABISConnection() == null) {
			printError("Core ABIS service not accessible");
			return;
		}

		if (!biographicService.testAvailable()) {
			printError("ABIS service not available");
			return;
		}

	}

	@Test
	public void enrollDuplicateExample() {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = BiographicServiceModule.getBiographicService();
		// standardEnrollExample(EnrollmentSubjectID);
		int subjectNumber = 2;

		String biographicId = String.valueOf(subjectNumber);

		if (!abisService.testAvailable()) {
			printError("ABIS service not available");
			return;
		}

		if (abisService.testABISConnection() == null) {
			printError("Core ABIS service not accessible");
			return;
		}

		if (!biographicService.testAvailable()) {
			printError("ABIS service not available");
			return;
		}

		SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference();
		String subjectDomain = enrollRef.getDomainName();
		String initialId = enrollRef.getSubjectID();
		if (subjectDomain != null) {
			enrollRef.setDomainName(null);
		}
		// Enroll from first sample
		enrollRef.setTargetFingers(SixFingers);
		EnrollmentUtils.enrollSubject(enrollRef, subjectNumber, 1, 1);

		SubjectEnrollmentReference duplicateRef = new SubjectEnrollmentReference();
		// Enroll from first sample
		duplicateRef.setTargetFingers(SixFingers);
		EnrollmentUtils.enrollSubject(duplicateRef, subjectNumber, 2, 1);
		String duplicateId = biographicId + "_dup";
		duplicateRef.setSubjectID(duplicateId);

		// Enroll the first subject
		if (!abisService.existsSubject(biographicId)) {
			MatchEngineResponse response = abisService.insertSubject(enrollRef, false);
			if (!response.isSuccess()) {
				printMessage("Handling REST response failure on insert of " + biographicId + " with error code "
						+ response.getStatusCode());
				handleRESTFailure(enrollRef, response, abisService);
				return;
			}
		}
		// Insert the biographic-id for this
		BiographicProfileRecord biographicRecord = EnrollmentUtils.getBiographicRecord(biographicId, "john", "Enroll");
		if (!biographicService.existsBiographicRecord(biographicId)) {
			boolean status = biographicService.insertBiographicRecord(biographicRecord);
			if (!status) {
				handleBiographicInsertFailure(biographicRecord);
				return;
			}
		}

		// Ready to go .. for duplicate test
		MatchEngineResponse dupResponse = abisService.insertSubject(duplicateRef, false);

		if (dupResponse.hasMatchResults()) {
			for (MatchResult result : dupResponse.getMatchResults()) {

				// Show biometric information
				printResult(result.getSubjectID(), result.getMatchScore());
				// Finger score details
				for (MatchResultDetail matchDetail : result.getMatchDetails()) {
					println(matchDetail.getIndex1() + " <==>" + matchDetail.getIndex2() + ":" + matchDetail.getScore());
				}

				// Access the matching biographic-id
				String subjectId = result.getSubjectID();
				// Obtain the information on the match
				BiographicProfileRecord matchRecord = biographicService.fetchBiographicRecord(subjectId);
				ImageBlob matchPortrait = matchRecord.getPortrait();
				String generalDetails = getBiographicManifest(matchRecord);
				printHeader("Biograpic Data for " + result.getSubjectID());
				println(generalDetails);
			}
		}

	}

	/**
	 * Functional test for identify
	 */
	@Test
	public void identifyExample() {
		String biographicId = String.valueOf(EnrollmentSubject);
		identifyExample(biographicId, TenFingers, false);
		identifyExample(biographicId, TenFingers, true);
	}
	
	public void identifyExample(String biographicId, int[] fingers) {
		identifyExample(biographicId, fingers);
	}

	public void identifyExample(String biographicId, int[] fingers, boolean faceEnabled) {
		int subjectNumber = Integer.valueOf(biographicId);
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = BiographicServiceModule.getBiographicService();

		String externalId = BiographicIdentifier.resolveExternalID(biographicId, abisService.getDomainName());

		checkSubjectEnrolled(biographicId);

		SubjectEnrollmentReference subject = EnrollmentUtils.enrollSubject(subjectNumber, fingers, 1, 1);
		if (faceEnabled) {
			EnrollmentUtils.enrollFacePortrait(subject);
		}
		subject.setSubjectID(null);
		MatchEngineResponse response = abisService.querySubject(subject, false);

		if (!response.isSuccess()) {
			printMessage("Handling REST response failure on query of " + subject.getRequestID() + " with error code "
					+ response.getStatusCode());
			handleRESTFailure(subject, response, abisService);
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

	/**
	 * Functional test for delete - test mode only
	 */
	@Test
	public void deleteSubjectExample() {
		TestABISService abisService = super.getTestABISService();
		String biographicId = String.valueOf(EnrollmentSubject);
		boolean existsSubject = abisService.existsSubject(biographicId);

		if (!existsSubject) {
			this.standardEnrollExample(biographicId);
		}

		// ! mark for delete
		abisService.deleteSubject(biographicId, true);

		// ! commit the deletes
		abisService.commitSubjectDeletes();

	}

	/**
	 * Functional test for verification
	 */
	@Test
	public void verifyExample() {
		verifyExampleFingersOnly();
		verifyExampleMultiModal();
	}
	
	public void verifyExampleFingersOnly() {
		doVerifyExample(EnrollmentSubjectID, RightHand, false);		
	}

	public void verifyExampleMultiModal() {
		doVerifyExample(EnrollmentSubjectID, RightHand, true);		
	}
	
	public void doVerifyExample(String biographicId, int[] fingers) {
		doVerifyExample(biographicId, fingers, PartnerExample.isFaceMatchEnabled());
	}
	
	public void doVerifyExample(String biographicId, int[] fingers, boolean faceEnabled) {
		checkSubjectEnrolled(biographicId);

		GenkeyABISService abisService = ABISServiceModule.getABISService();
		int subjectId = Integer.parseInt(biographicId);

		SubjectEnrollmentReference enrollmentRef = EnrollmentUtils.enrollSubject(subjectId, fingers, 1, 1);
		if (faceEnabled) {
			EnrollmentUtils.enrollFacePortrait(enrollmentRef);
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

	@Test
	public void imageFetchExample() {
		doImageFetchExample(EnrollmentSubjectID, IndexFingers);
	}

	private void doImageFetchExample(String biographicId, int[] fingers) {
		checkSubjectEnrolled(biographicId);

		GenkeyABISService abisService = ABISServiceModule.getABISService();
		ImageRequestResponse response = abisService.getImageRequest(biographicId, fingers);
		boolean success = response.isSuccess();

		if (success) {
			String result = response.getOperationResult();
			printResult("Operation results", result);
			ImageWrapperSet wrapper = response.getImageWrapper();

			int resoution = wrapper.getResolution();

			// Iterate through the fingers
			for (int finger : wrapper.keySet()) {
				// Access the image blob
				ImageBlob imageBlob = wrapper.get(finger);
				int resolution = wrapper.getResolution(finger);
				printResult("Image " + finger, imageBlob.getImageEncoding().length + "/" + imageBlob.getImageFormat());

				// Obtain image in BMP format
				ImageData imageData = new ImageData(imageBlob.getImageEncoding(), imageBlob.getImageFormat(),
						resolution);
				byte[] bmpEncoding = imageData.asEncodedImage(ImageData.FORMAT_BMP);

				// Further processing with the bmpEncoding.

			}
		} else {
			String message = response.getErrorMessage();
			String result = response.getOperationResult();
			printResult("Operation results", result);
			printResult("Error", message);
		}

	}

	private void checkSubjectEnrolled(String biographicId) {
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		boolean existsSubject = abisService.existsSubject(biographicId);
		if (!existsSubject) {
			this.standardEnrollExample(biographicId);
		}

	}

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
		EnrollmentUtils.enrollSubject(subject, subjectNumber, 1, 1);

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
	


}
