package com.genkey.partner.workshop;

import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.service.params.EnquireStatus.CompletionStatus;

public class TraumaProfileExample extends IncrementalEnrolTests {

  public static void main(String[] args) {
    TraumaProfileExample test = new TraumaProfileExample();
    test.processCommandLine(args);
  }

  @Override
  protected void runAllExamples() {
    traumaProfileTest();
  }

  public void traumaProfileTest() {
    traumaProfileTest(TestSubjectID2);
  }

  public void traumaProfileTest(String subjectID) {

    GenkeyABISService abisService = this.getAbisService();

    if (!abisService.testAvailable()) {
      return;
    }

    // status before enrolment
    EnquireStatus currentStatus = abisService.enquireSubject(subjectID);
    if (currentStatus != null) {
      showTraumaStatus(currentStatus);
      if (currentStatus.existsSubject() && currentStatus.isAfisPresent()) {
        printMessageF("This test does not apply to existing subject %s with fingers", subjectID);
        return;
      }
    }

    // Check status after enrolling face

    if (!currentStatus.isFaceTemplatePresent()) {
      performEnrolNextStep(subjectID, EnrollmentStep.Face);
      currentStatus = abisService.enquireSubject(subjectID);
      showTraumaStatus(currentStatus);
    }

    // Set permanent trauma profile to right hand
    int[] missingFingers = EnquireStatus.RightHand;
    currentStatus = abisService.setFingerTraumaProfile(subjectID, missingFingers);
    assertContentEqual(currentStatus.getTraumaFingers(), missingFingers);

    // Set qualit profile to left thumb - 
    int [] leftThumb = {6};    
    currentStatus = abisService.setFingerQualityProfile(subjectID, leftThumb);    
    assertContentEqual(currentStatus.getLowQualityFingers(), leftThumb);

    // Set temporary trauma profile to right thumb
    int [] rightThumb = {1};
    currentStatus = abisService.setFingerTemporaryProfile(subjectID, rightThumb);    
    assertContentEqual(currentStatus.getTemporaryTraumaFingers(), rightThumb);    

    // how the new status
    currentStatus = abisService.enquireSubject(subjectID);
    showTraumaStatus(currentStatus);

    // Get an enrollment hint - should be for LeftHand
    int[] fingers = currentStatus.askEnrolmentHint();
    if (!checkContentEqual(fingers, EnquireStatus.LeftHand)) {
      printError("Unexpected result on enrolment hint");
    }

    performEnrolNextStep(subjectID, EnrollmentStep.LeftHand);
    currentStatus = abisService.enquireSubject(subjectID);
    showTraumaStatus(currentStatus);
  }

  private void showTraumaStatus(EnquireStatus currentStatus) {

    int[] qualityProfile = currentStatus.getLowQualityFingers();
    int[] traumaProfile = currentStatus.getTraumaFingers();
    int [] tempProfile = currentStatus.getTemporaryTraumaFingers();

    printResult("Quality profile", qualityProfile);
    printResult("Trauma profile", traumaProfile);
    printResult("Temp profile", tempProfile);

    CompletionStatus completionStatus = currentStatus.enquireFingerStatus(EnquireStatus.TenFingers);
    printResult("Completion status", completionStatus);

    // default result for missing fingers excludes the profiles
    int[] missingFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers);
    printResult("Missing fingers ", missingFingers);

    // Explicit result for missing fingers specify exclude low quality
    int[] missingFingers2 = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false);

    // Explicit result for missing fingers specify exclude low quality and all trauma
    int[] missingFingers3 =
        currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false, false, false);

    // show these results are the same
    assertContentEqual(missingFingers, missingFingers2);
    assertContentEqual(missingFingers, missingFingers3);

    printResult("Missing fingers (exclude all)", missingFingers);

    // Missing fingers including low quality
    int[] missingQFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true);

    // Equivalent call specifically excluding trauma fingers
    int[] missingQFingers2 =
        currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true, false, false);
    assertContentEqual(missingQFingers, missingQFingers2);

    printResult("Missing fingers including low quality ", missingQFingers);
    
    // Missing fingers including temporary trauma fingers
    int[] missingTFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false, true, false);
    printResult("Missing fingers including temporary trauma ", missingTFingers);

    
    // Missing fingers without any exceptions for quality or trauma
    int[] allMissingFingers =
        currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true, true, true);
    printResult("Missing fingers no exclusions ", allMissingFingers);
  }
}
