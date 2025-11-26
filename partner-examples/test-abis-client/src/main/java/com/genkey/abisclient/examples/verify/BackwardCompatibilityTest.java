package com.genkey.abisclient.examples.verify;

import com.genkey.abisclient.ABISClientException;
import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceMatcher;
import com.genkey.abisclient.verification.VerifyResult;
import java.util.ArrayList;
import java.util.List;

public class BackwardCompatibilityTest extends ExampleModule {

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
    //		verificationVersionFailureTest();
    // Start here
    verificationTest();
    verificationTest2();
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
    int version =
        useLegacy ? ABISClientLibrary.getLegacyVersion() : ABISClientLibrary.getLatestVersion();
    ABISClientLibrary.activateVersion(version, false);
    // Note this will generate on demand so the test will work even if no legacy data is available
    AnonymousFusionReference biohash =
        TestDataManager.getAnonymousReference(testSubject, useLegacy, true);
    verificationTest(biohash, testSubject);
  }

  private void verificationTest(AnonymousFusionReference biohash, int testSubject) {

    // Interrogate the token to determine if it is legacy
    // boolean isLegacy = biohash.isLegacy();

    // printObject("BioHASH", biohash.printReferenceState());
    int[] fingers1 = biohash.getFingers();

    int version = biohash.getAlgorithmVersion();

    // We need to configure system to use compatible algorithms for verification
    // All future calls to ImageContext will work in legacy mode until this change is reversed
    // ImageContextSDK.setLegacyVerificationMode(isLegacy);
    ABISClientLibrary.setEnrollmentVersion(version);

    // Interrogate for fingers
    int[] fingers = biohash.getFingers();

    // Short cut to capture and generate references
    List<ReferenceDataItem> references = TestDataManager.loadReferences(testSubject, fingers, 2);
    ReferenceDataItem proto = references.get(0);
    // printObject("Proto", proto.printReferenceState());

    // Instantiate a matcher
    AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();

    // Run the verification.  The matcher will know what to do.
    VerifyResult status = matcher.verify(references, biohash, 60);

    if (status.isSuccess()) {
      int score = status.getScore();
      printResult("Match score", score);
    }
  }

  public void verificationTest2() {
    // Runs tests against BioHASH generated from prior version of Library
    verificationTest2(true, 1);
    verificationTest2(true, 2);
    verificationTest2(true, 3);

    // Runs tests against BioHASH generated from this version of the library
    verificationTest2(false, 1);
    verificationTest2(false, 2);
    verificationTest2(false, 3);
  }

  private void verificationTest2(boolean useLegacy, int testSubject) {
    int version =
        useLegacy ? ABISClientLibrary.getLegacyVersion() : ABISClientLibrary.getLatestVersion();
    ABISClientLibrary.activateVersion(version, false);
    // Note this will generate on demand so the test will work even if no legacy data is available
    AnonymousFusionReference biohash =
        TestDataManager.getAnonymousReference(testSubject, useLegacy, true);
    verificationTest2(biohash, testSubject);
  }

  private void verificationTest2(AnonymousFusionReference biohash, int testSubject) {

    // Interrogate the token to determine if it is legacy
    // boolean isLegacy = biohash.isLegacy();

    //		printObject("BioHASH", biohash.printReferenceState());
    int[] fingers1 = biohash.getFingers();

    int version = biohash.getAlgorithmVersion();

    // We need to configure system to use compatible algorithms for verification
    // All future calls to ImageContext will work in legacy mode until this change is reversed
    // ImageContextSDK.setLegacyVerificationMode(isLegacy);

    // Interrogate for fingers
    int[] fingers = biohash.getFingers();

    // Load verification references for sampleIndex 2 with the algorithmVersion from the bioHash
    List<ReferenceDataItem> references =
        loadVerificationReferences(testSubject, fingers, 2, version);

    // Instantiate a matcher
    AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();

    // Run the verification.  The matcher will know what to do.
    VerifyResult status = matcher.verify(references, biohash, 60);

    if (status.isSuccess()) {
      int score = status.getScore();
      printResult("Match score", score);
    }
  }

  public void verificationVersionFailureTest() {
    verificationVersionFailureTest(true, 1);
    verificationVersionFailureTest(false, 1);
  }

  public void verificationVersionFailureTest(boolean useLegacy, int testSubject) {
    AnonymousFusionReference biohash =
        TestDataManager.getAnonymousReference(testSubject, useLegacy, true);
    verificationFalureTestTest(biohash, testSubject);
  }

  public void verificationFalureTestTest(AnonymousFusionReference biohash, int testSubject) {
    boolean isLegacy = biohash.isLegacy();
    int version = biohash.getAlgorithmVersion();
    // Find the incompatible supported version to current setting of isLegacy
    int badVersion = ABISClientLibrary.getDefaultVersion(!isLegacy);
    // ImageContextSDK.setLegacyVerificationMode(failureMode);
    // ABISClientLibrary.setVerificationVersion()

    // Interrogate for fingers
    int[] fingers = biohash.getFingers();

    // Short cut to capture and generate references
    List<ReferenceDataItem> referencesBad =
        loadVerificationReferences(testSubject, fingers, 2, badVersion);
    List<ReferenceDataItem> referencesGood =
        loadVerificationReferences(testSubject, fingers, 2, version);

    // Instantiate a matcher
    AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();

    // Run the verification.  The matcher will know what to do.
    boolean exceptionThrown = false;

    try {
      VerifyResult status = matcher.verify(referencesGood, biohash, 60);
      printResult("Result Good", status);

      VerifyResult status2 = matcher.verify(referencesBad, biohash, 60);
      printResult("Result Bad", status2);

    } catch (ABISClientException e) {
      int code = e.getErrorCode();
      String message = e.getMessage();
      String fullDesc = e.getErrorDescription(code);
      //			e.printStackTrace();
      exceptionThrown = true;
      super.printMessage(
          "Expected exception has been thrown Code:"
              + code
              + "\n"
              + "Short message:"
              + message
              + "\n"
              + "Full message:"
              + fullDesc);
    }

    if (!exceptionThrown) {
      super.printMessage("Unexpected absence of error on backwards compatibiluty test");
    }
  }

  private List<ReferenceDataItem> loadVerificationReferences(
      int testSubject, int[] fingers, int sampleIndex, int version) {
    List<String> imageFiles = TestDataManager.getImageFiles(testSubject, fingers, sampleIndex);
    List<ReferenceDataItem> references = new ArrayList<ReferenceDataItem>();
    for (int ix = 0; ix < imageFiles.size(); ix++) {
      String imageFile = imageFiles.get(ix);
      int fingerId = fingers[ix];
      ImageData image = TestDataManager.loadImage(imageFile);
      boolean verificationMode = true;

      // Instantiate imageContext with version information and indication to use verification mode
      // as true
      ImageContext context = new ImageContext(image, fingerId, version, verificationMode);

      // ReferenceData is generated with version and mode specified in constructor
      ReferenceDataItem reference = context.getReferenceData();

      references.add(reference);
    }
    return references;
  }
}
