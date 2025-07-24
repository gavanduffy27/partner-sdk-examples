package com.genkey.abisclient.examples.verify;

import java.util.List;

import com.genkey.abisclient.ABISClientException;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceMatcher;
import com.genkey.abisclient.verification.VerifyResult;

public class BackwardCompatibilityTest extends ExampleModule{
	
	

	@Override
	protected void setUp() {
		// Configure availabiltiy of backwards compatibility algorithm libraries
		ImageContextSDK.enableBackwardsCompatibility();
		
		// This is to stop reference caching which would cause this test to break because they
		// are not reusable when the algorithms are changing
		TestDataManager.setCacheEnabled(false);
	}

	@Override
	protected void runAllExamples() {
		verificationTest();
		verificationVersionFailureTest();
	}

	public void verificationTest() {
		// Runs tests against BioHASH generated from prior version of Library
		verificationTest(true, 1);
		verificationTest(true, 2);
		verificationTest(true, 3);

		// Runs tests against BioHASH generated from this version of the library
		verificationTest(false, 1);
		verificationTest(false, 2);
		verificationTest(false, 3);

	}

	private void verificationTest(boolean useLegacy, int testSubject) {
		// Note this will generate on demand so the test will work even if no legacy data is available
		AnonymousFusionReference biohash = TestDataManager.getAnonymousReference(testSubject, useLegacy, true);
		verificationTest(biohash, testSubject);
	}

	private void verificationTest(AnonymousFusionReference biohash, int testSubject) {
		
		// Interrogate the token to determine if it is legacy
		boolean isLegacy = biohash.isLegacy();
		
		// We need to configure system to use compatible algorithms for verification
		// All future calls to ImageContext will work in legacy mode until this change is reversed
		ImageContextSDK.setLegacyVerificationMode(isLegacy);
		
		// Interrogate for fingers
		int [] fingers = biohash.getFingers();
		
		// Short cut to capture and generate references
		List<ReferenceDataItem> references = TestDataManager.loadReferences(testSubject, fingers, 1);
		
		// Instantiate a matcher
		AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();
		
		// Run the verification.  The matcher will know what to do.
		VerifyResult status = matcher.verify(references, biohash, 60);
		
		if(status.isSuccess()) {
			int score = status.getScore();
			printResult("Match score", score);
		}
		
		// Restore the system back to default enrollment mode. This reverses the action from setLegacyVerificationMode ..
		ImageContextSDK.setDefaultVerificationMode();
		
	}

	public void verificationVersionFailureTest() {
		verificationVersionFailureTest( true, 1);
		verificationVersionFailureTest( false, 1);		
	}
	
	public void verificationVersionFailureTest(boolean useLegacy, int testSubject) {
		AnonymousFusionReference biohash = TestDataManager.getAnonymousReference(testSubject, useLegacy, true);
		verificationFalureTestTest(biohash, testSubject);
	}
	
	public void verificationFalureTestTest(AnonymousFusionReference biohash, int testSubject) {
		// Interrogate the token to determine if it is legacy
		boolean isLegacy = biohash.isLegacy();
		
		boolean failureMode = !isLegacy;
		ImageContextSDK.setLegacyVerificationMode(failureMode);
		
		// Interrogate for fingers
		int [] fingers = biohash.getFingers();
		
		// Short cut to capture and generate references
		List<ReferenceDataItem> references = TestDataManager.loadReferences(testSubject, fingers, 1);
		
		// Instantiate a matcher
		AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();
		
		// Run the verification.  The matcher will know what to do.
		boolean exceptionThrown = false;
		
		try {
			VerifyResult status = matcher.verify(references, biohash, 60);
		} catch (ABISClientException e) {
			int code = e.getErrorCode();
			String message = e.getMessage();
			String fullDesc = e.getErrorDescription(code);
//			e.printStackTrace();
			exceptionThrown=true;
			super.printMessage("Expected exception has been thrown Code:" + code + "\n" 
								+ "Short message:" + message + "\n"
								+  "Full message:" + fullDesc);
		}

		if ( !exceptionThrown) {
			super.printMessage("Unexpected absence of error on backwards compatibiluty test");
		}
		
		// Restore the system back to default enrollment mode. This reverses the action from setLegacyVerificationMode ..
		ImageContextSDK.setDefaultVerificationMode();
		
		
	}

	
}
