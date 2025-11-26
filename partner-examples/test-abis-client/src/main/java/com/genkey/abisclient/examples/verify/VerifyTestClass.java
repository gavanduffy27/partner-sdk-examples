package com.genkey.abisclient.examples.verify;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.MainTestRunner;
import com.genkey.abisclient.examples.utils.ExampleSettings;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceExtractor;
import com.genkey.abisclient.verification.AnonymousReferenceMatcher;
import com.genkey.abisclient.verification.PolicyVerifyResult;
import com.genkey.abisclient.verification.SignatureGenerateResult;
import com.genkey.abisclient.verification.SignatureVerifyResult;
import com.genkey.abisclient.verification.VerificationEnums.BioHASHGenerationMode;
import com.genkey.abisclient.verification.VerificationPolicy;
import com.genkey.abisclient.verification.VerificationSDK;
import com.genkey.abisclient.verification.VerifyResult;
import com.genkey.platform.utils.ArrayIterator;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifyTestClass extends ExampleModule {

  AnonymousFusionReference biohash = null;

  AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();

  AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();

  @Override
  protected void setUp() {
    super.setUp();
    int bskKey = (int) ImageContextSDK.getThreshold(GKThresholdParameter.ServerEmulationDomain);
    int requiredKey = getPropertyMap().getPropertyInt(MainTestRunner.ARG_SERVER_DOMAIN);
    boolean keyFault = requiredKey != bskKey;
    if (keyFault) {
      super.promptOperatorContinue("Unexpected key setting for BSK");
      ImageContextSDK.setThreshold(GKThresholdParameter.ServerEmulationDomain, requiredKey);
      int bskKey2 = (int) ImageContextSDK.getThreshold(GKThresholdParameter.ServerEmulationDomain);
      printMessage("Key setting is changed from " + bskKey + " to " + bskKey2);
    }
    BioHASHGenerationMode mode = ExampleSettings.getInstance().getGenerationMode();
    VerificationSDK.setBioHASHGenerationMode(mode);
  }

  @Override
  protected void runAllExamples() {

    // referenceReplayTest();

    // Start from here
    testKeyGenerate();
    testStandardOSFVerify();
    testStandardVerify();
    testImpostorVerify();
    testPolicyVerify();
    testSignatureVerify();

    // Everything in one big routine
    testBiohashExamples();
  }

  public void referenceReplayTest() {
    String testDirectory = FileUtils.expandConfigPath("replay/hajo/references");
    String[] files = FileUtils.getFilenames(testDirectory, "dat", true);
    if (files == null) {
      return;
    }
    List<ReferenceDataItem> referenceItems = new ArrayList<>();
    List<ReferenceDataItem> referenceItemsMod = new ArrayList<>();
    for (String file : files) {
      try {
        String baseName = FileUtils.baseName(file);
        int finger = Integer.parseInt(Commons.getLastToken(baseName, "_"));
        byte[] refData = FileUtils.byteArrayFromFile(file);
        ReferenceDataItem refItemMod = new ReferenceDataItem(refData, finger);
        ReferenceDataItem refItem = new ReferenceDataItem(refData);
        referenceItems.add(refItem);
        referenceItemsMod.add(refItemMod);
        printObject("RefItem", refItem.printReferenceState());
        printObject("RefItemMod", refItemMod.printReferenceState());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();

    AnonymousFusionReference biohash = extractor.createFromReferences(referenceItems, true, null);
    int[] fingers = biohash.getFingers();
    printResult("Biohash fingers", printArray(fingers));
    printResult("biohash size", biohash.getData().length);
    printObject("BioHash", biohash.printReferenceState());

    AnonymousFusionReference biohashMod =
        extractor.createFromReferences(referenceItemsMod, true, null);
    printResult("Biohash fingers Mod", printArray(biohashMod.getFingers()));
    printResult("biohash mod size", biohashMod.getData().length);
    printObject("BioHash Mod", biohashMod.printReferenceState());
  }

  public void runSelectedExamples() {
    hajoReplayTest();
    // testKeyGenerate();
    /*
    testStandardOSFVerify();
    testStandardVerify();
    testImpostorVerify();
    testPolicyVerify();
    testSignatureVerify();

    // Everything in one big routine
    testBiohashExamples();
    */
  }

  static int[] getTestFingers() {
    return Commons.generateRangeV(3, 4, 7, 8);
  }

  protected AnonymousFusionReference getBiohash() {
    if (this.biohash == null) {
      // This test will recreate this if running tests out of sequence
      testKeyGenerate();
    }
    return this.biohash;
  }

  public void hajoReplayTest() {
    ABISClientLibrary.activateVersion(4, true);
    String testDirectory = FileUtils.expandConfigPath("replay/hajo/problem1/references");
    String exportDirectory = FileUtils.mkFilePath(testDirectory, "exports");

    String[] files = FileUtils.getFilenames(testDirectory, "dat", true);
    List<ReferenceDataItem> referenceItems = new ArrayList<>();
    List<ReferenceDataItem> referenceItemsMod = new ArrayList<>();
    for (String file : files) {
      try {
        String baseName = FileUtils.baseName(file);
        int finger = Integer.parseInt(Commons.getLastToken(baseName, "_"));
        byte[] refData = FileUtils.byteArrayFromFile(file);
        ReferenceDataItem refItemMod = new ReferenceDataItem(refData, finger);
        ReferenceDataItem refItem = new ReferenceDataItem(refData);
        referenceItems.add(refItem);
        referenceItemsMod.add(refItemMod);
        printObject("RefItem", refItem.printReferenceState());
        printObject("RefItemMod", refItemMod.printReferenceState());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();

    boolean skipRepeats = false;
    if (!skipRepeats) {
      AnonymousFusionReference biohash = extractor.createFromReferences(referenceItems, true, null);
      int[] fingers = biohash.getFingers();
      printResult("Biohash fingers", printArray(fingers));
      printResult("biohash size", biohash.getData().length);
      printObject("BioHash", biohash.printReferenceState());
      exportTemplateReference(biohash, "biohash2", exportDirectory);
    }
    AnonymousFusionReference biohashMod =
        extractor.createFromReferences(referenceItemsMod, true, null);
    printResult("Biohash fingers Mod", printArray(biohashMod.getFingers()));
    printResult("biohash mod size", biohashMod.getData().length);
    printObject("BioHash2 Mod", biohashMod.printReferenceState());
    exportTemplateReference(biohashMod, "biohash2Mod", exportDirectory);
  }

  static void exportTemplateReference(
      AnonymousFusionReference fusionRef, String name, String exportPath) {
    String exportFile = FileUtils.expandConfigFile(exportPath, name, "dat");
    byte[] encoding = fusionRef.getData();
    try {
      FileUtils.byteArrayToFile(encoding, exportFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static String printArray2(int[] values) {
    return CollectionUtils.containerToString(new ArrayIterator<Integer>(values));
  }

  public void testKeyGenerate() {
    int testSubject = 1;
    int[] testFingers = getTestFingers();
    printMessage("Loading refs");
    List<ReferenceDataItem> enrolList = TestDataManager.loadReferences(testSubject, testFingers, 1);
    // Create using default entropy
    printMessage("Creating biohash");
    AnonymousFusionReference biohash1 = extractor.createFromReferences(enrolList, true, null);

    // Create with supplied entropy.
    byte[] myEntropy = Commons.stringToByteArray("My secret inputs");
    AnonymousFusionReference biohash2 = extractor.createFromReferences(enrolList, true, myEntropy);

    // Cache biohash to avoid time expensive regeneration on other tests
    this.biohash = biohash2;

    int[] fingers = biohash1.getFingers();
    int[] fingers2 = biohash2.getFingers();

    printResult("Fingers", fingers);
    printResult("Fingers2", fingers2);

    byte[] encoding = biohash1.getData();

    AnonymousFusionReference biohash3 = new AnonymousFusionReference(encoding);
    int[] fingers3 = biohash3.getFingers();
    printResult("Fingers3", fingers3);
  }

  public void testStandardVerify() {
    int testSubject = 1;
    int[] testFingers = getTestFingers();

    // As generated in previous test
    AnonymousFusionReference biohash = this.getBiohash();

    // Simulates an enrolment from test images. A collection of ReferenceDataItem one for each
    // finger
    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 1);

    // test against default threshold
    VerifyResult result = matcher.verify(testRefs, biohash);

    // It is printable
    printResult("Genuine test", result);

    // it contains the result as pass/fail
    boolean didItMatch = result.isSuccess();

    // It also holds the score in -10 log10(far) conventions
    int score = result.getScore();

    printResult("Score", score);
    printResult("Success", didItMatch);
  }

  public void testStandardOSFVerify() {
    int testSubject = 1;
    int[] testFingers = getTestFingers();

    // As generated in previous test
    AnonymousFusionReference biohash = this.getBiohash();

    // Simulates an enrolment from test images. A collection of ReferenceDataItem one for each
    // finger
    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 1);

    // Test against specified threshold of 1E-5
    int osfThreshold = 50;
    VerifyResult result3 = matcher.verify(testRefs, biohash, osfThreshold);
    printResult("Result with call threshold", result3);

    // Repeat on different orders
    for (int shift = 1; shift < testRefs.size() - 1; shift++) {
      List<ReferenceDataItem> shuffleRefs = shuffleList(testRefs, shift);
      VerifyResult result4 = matcher.verify(shuffleRefs, biohash, osfThreshold);
      printResult("Result with shuffle list " + shift, result4);
    }
  }

  public static <T> List<T> shuffleList(List<T> list, int shift) {
    List<T> result = CollectionUtils.newList();
    int nElements = list.size();
    for (int ix = 0; ix < nElements; ix++) {
      int srcIx = cycleShift(ix, shift, nElements);
      result.add(list.get(srcIx));
    }
    return result;
  }

  private static int cycleShift(int ix, int shift, int nElements) {
    return (ix + shift) % nElements;
  }

  public void testOSFWithThresholds() {
    int testSubject = 1;
    int[] testFingers = getTestFingers();

    // As generated in previous test
    AnonymousFusionReference biohash = this.getBiohash();

    // Simulates an enrolment from test images. A collection of ReferenceDataItem one for each
    // finger
    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 1);

    // Check and test with the default threshold
    int defaultThreshold = VerificationSDK.getFARScoreThreshold();
    printResult("DefaultThreshold", defaultThreshold);
    VerifyResult result1 = matcher.verify(testRefs, biohash);
    printResult("Result with standard threshold", result1);

    // Change and test against new default threshold
    VerificationSDK.setFARScoreThreshold(60);
    VerifyResult result2 = matcher.verify(testRefs, biohash);
    printResult("Result with new default", result2);

    // Test against specified threshold of 1E-5
    VerifyResult result3 = matcher.verify(testRefs, biohash, 50);
    printResult("Result with call threshold", result3);

    // Perform OSF threshold using a policy with no per finger threshold
    int osfThreshold = 35;
    VerificationPolicy policyOSF = new VerificationPolicy();
    policyOSF.setFusionScoreThreshold(osfThreshold);
    policyOSF.setFingerScoreThreshold(0);

    PolicyVerifyResult result = matcher.verifyWithPolicy(testRefs, biohash, policyOSF);

    // As with other results, the toString method provides descriptive summary of all state
    printResult("PolicyResult", result);
  }

  public void testImpostorVerify() {
    AnonymousFusionReference biohash = this.getBiohash();

    int impostorSubject = 3;
    List<ReferenceDataItem> impostorRefs =
        TestDataManager.loadReferences(impostorSubject, getTestFingers(), 1);

    VerifyResult result = matcher.verify(impostorRefs, biohash);

    // Just print the result. The toString method formats its state.
    printResult("Impostor test", result);
  }

  public void testPolicyVerify() {
    int testSubject = 1;
    int[] testFingers = getTestFingers();

    AnonymousFusionReference biohash = this.getBiohash();

    // Note these are auto-cached by the TestDataManager
    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 1);

    // Obtain different policies

    VerificationPolicy policyLax = new VerificationPolicy();
    policyLax.setFusionScoreThreshold(40);
    policyLax.setFingerScoreThreshold(20);
    policyLax.setTolerance(0);

    // Equalivalent to abpve would be to use the constructor short cut
    policyLax = new VerificationPolicy(40, 20);

    // Strict quality requirements. Want 1E-6 discrimation on each finger
    VerificationPolicy policyStrict = new VerificationPolicy(120, 60);

    VerificationPolicy superStrictOsf = new VerificationPolicy(10000, 60);

    VerificationPolicy strictFinger = new VerificationPolicy(10000, 300);

    // Pure QA check on fingers. Enforces only that each finger matches with discrimination of 1E-3
    VerificationPolicy policyQAOnly = new VerificationPolicy(0, 30);

    testPolicyBasedCheck("Policy Lax", testRefs, biohash, policyLax, true);
    testPolicyBasedCheck("Strict", testRefs, biohash, policyStrict, true);
    testPolicyBasedCheck("QA only", testRefs, biohash, policyQAOnly, true);
    testPolicyBasedCheck("Strict osf", testRefs, biohash, superStrictOsf, false);
    testPolicyBasedCheck("Strict finger", testRefs, biohash, strictFinger, false);
  }

  private void testPolicyBasedCheck(
      String testName,
      List<ReferenceDataItem> references,
      AnonymousFusionReference biohash,
      VerificationPolicy policy,
      boolean isAuthentic) {

    printHeader("Policy Test " + testName + " for " + (isAuthentic ? "Genuine" : "Impostor"));

    PolicyVerifyResult result = matcher.verifyWithPolicy(references, biohash, policy);

    // As with other results, the toString method provides descriptive summary of all state
    printResult("PolicyResult", result);

    // We can test if we passed
    boolean allIsGood = result.isSuccess();
    printResult("Good enrolment", allIsGood);

    // We can confirm that this IS a position dependent check
    boolean isOrderImportant = result.isPositionDependent();
    printResult("PositionDependent", isOrderImportant);

    // We can look at the overall verification score
    int log10Score = result.getScore();
    printResult("Match score", log10Score);

    // We can view the per finger scores
    Map<Integer, Integer> fingerScores = result.getFingerScores();

    for (Map.Entry<Integer, Integer> fingerScore : fingerScores.entrySet()) {
      int finger = fingerScore.getKey();
      int score = fingerScore.getValue();
      printIndexResult("Finger", finger, score);
    }

    // We can test for expected result
    if (result.isSuccess() != isAuthentic) {
      printMessage("Unexpected result on policy verification");
    }
  }

  public void testSignatureVerify() {
    // Same old
    int testSubject = 1;
    int[] testFingers = getTestFingers();

    // Obtain pre-cached biohash and references
    AnonymousFusionReference biohash = this.getBiohash();
    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 2);

    // Also for impostor
    int impostorSubject = 2;
    List<ReferenceDataItem> impostorRefs =
        TestDataManager.loadReferences(impostorSubject, getTestFingers(), 1);

    testSignatureChecks(testRefs, biohash, true);
    testSignatureChecks(impostorRefs, biohash, false);
  }

  /**
   * @param references Basic references as extracted using enrolment routines
   * @param biohash Generated BioHASH
   * @param isAuthentic Set to true if we know the references and biohash are from same subject
   */
  private void testSignatureChecks(
      List<ReferenceDataItem> references, AnonymousFusionReference biohash, boolean isAuthentic) {

    printHeader("Signature Test for " + (isAuthentic ? "Genuine" : "Impostor"));

    // Instantiate the same matcher as we use for verifications
    AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();

    // Signing data
    byte[] binaryMessage = Commons.stringToByteArray("A message to be signed");

    // Tampered data
    byte[] tamperMessage = Commons.stringToByteArray("A massage to be sIgn3d");

    // Generate signature
    SignatureGenerateResult result = matcher.signatureGenerate(references, biohash, binaryMessage);

    // We can display the state directly
    printResult("Signature result", result);

    // Did the operation succeed ?
    if (!result.isSuccess()) {
      printMessage("Failed to generate signature because of biometric failure ");
      return;
    }

    // Also we pick up the signature itself from the result data
    byte[] signature = result.getSignature();

    /** Verify section */

    // Similar arguments as for sign except we pass through the pregenerated signature
    SignatureVerifyResult verifyResult =
        matcher.signatureVerify(references, biohash, binaryMessage, signature);
    printResult("Signature Verify Result", verifyResult);

    // Check for success
    if (!verifyResult.isSuccess()) {
      // two possibilities
      if (!verifyResult.isUserVerified()) {
        printMessage("Signature check failed because of biometric check");
      } else if (!verifyResult.isSignatureVerified()) {
        printMessage("Signature check failed because message has changed");
      }
    }

    // To show what happens with a tampered message
    SignatureVerifyResult verifyResult2 =
        matcher.signatureVerify(references, biohash, tamperMessage, signature);
    printResult("Tamper result", verifyResult2);

    if (verifyResult2.isSignatureVerified() == false) {
      printMessage("Expected failure on tampered message");
    } else {
      printMessage("Free beers for 6 months");
    }
  }

  /** Long test that does everything in a single long narrative. */
  public void testBiohashExamples() {
    int testSubject = 1;
    int impostorSubject = 2;
    int[] testFingers = getTestFingers();
    printMessage("Loading refs");
    List<ReferenceDataItem> enrolList = TestDataManager.loadReferences(testSubject, testFingers, 1);
    AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();

    // Create using default entropy
    printMessage("Creating biohash");
    AnonymousFusionReference biohash = extractor.createFromReferences(enrolList, true, null);

    List<ReferenceDataItem> testRefs = TestDataManager.loadReferences(testSubject, testFingers, 1);
    List<ReferenceDataItem> impostorRefs =
        TestDataManager.loadReferences(impostorSubject, testFingers, 1);

    // Create with supplied entropy
    byte[] myEntropy = Commons.stringToByteArray("My secret inputs");
    AnonymousFusionReference biohash2 = extractor.createFromReferences(enrolList, true, myEntropy);

    // Perform a standard validation
    AnonymousReferenceMatcher matcher = new AnonymousReferenceMatcher();
    VerifyResult result = matcher.verify(enrolList, biohash);
    this.printResult("Enrol sef test", result);

    // This time with references from a different presentation
    VerifyResult result2 = matcher.verify(testRefs, biohash);
    this.printResult("Genuine test", result2);

    // This time with an impostor
    VerifyResult result3 = matcher.verify(impostorRefs, biohash);
    this.printResult("Impostor test result", result3);

    // Now we test the policy based matching
    VerificationPolicy policyLax = new VerificationPolicy(40, 20);

    VerificationPolicy policyStrict = new VerificationPolicy(120, 60);

    VerificationPolicy policyQAOnly = new VerificationPolicy(0, 30);

    testPolicyBasedCheck("Policy Lax", testRefs, biohash, policyLax, true);

    testPolicyBasedCheck("Policy Lax", impostorRefs, biohash, policyLax, false);

    testPolicyBasedCheck("Strict", testRefs, biohash, policyQAOnly, true);

    testSignatureChecks(testRefs, biohash, true);
    testSignatureChecks(impostorRefs, biohash, false);
  }
}
