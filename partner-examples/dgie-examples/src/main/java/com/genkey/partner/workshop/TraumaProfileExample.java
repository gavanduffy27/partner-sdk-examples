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
    traumaProfileTest(TestSubjectID);
  }

  public void traumaProfileTest(String subjectID) {

    GenkeyABISService abisService = this.getAbisService();

    if (!abisService.testAvailable()) {
      return;
    }
    
    

    // status before enrolment 
    EnquireStatus currentStatus = abisService.enquireSubject(subjectID);
    showTraumaStatus(currentStatus);
    
    if (currentStatus.existsSubject() && currentStatus.isAfisPresent())  {
    	printMessageF("This test does not apply to existing subject %s with fingers", subjectID);
    	return;
    }
    
    
    // Check status after enrolling face
    
    if (! currentStatus.isFaceTemplatePresent()) {
        performEnrolNextStep(subjectID, EnrollmentStep.Face);  
        currentStatus = abisService.enquireSubject(subjectID);
        showTraumaStatus(currentStatus);    	
    }
    

    
    // Set trauma and quality profiles to RightHand and thumbs respectively
    int[] missingFingers = EnquireStatus.RightHand;
    currentStatus = abisService.setFingerTraumaProfile(subjectID, missingFingers);
    assertContentEqual(currentStatus.getTraumaFingers(), missingFingers);
    currentStatus = abisService.setFingerQualityProfile(subjectID, EnquireStatus.Thumbs);
    assertContentEqual(currentStatus.getLowQualityFingers(), EnquireStatus.Thumbs);
    
    currentStatus = abisService.enquireSubject(subjectID);
    showTraumaStatus(currentStatus);

    // Get an enrollment hint - should be for LeftHand
    int [] fingers = currentStatus.askEnrolmentHint();    
    if (! checkContentEqual(fingers, EnquireStatus.LeftHand)) {
    	printError("Unexpected result on enrolment hint");
    }
    
    performEnrolNextStep(subjectID, EnrollmentStep.LeftHand);
    currentStatus = abisService.enquireSubject(subjectID);
    showTraumaStatus(currentStatus);
    
    
  }

  private void showTraumaStatus(EnquireStatus currentStatus) { 

	  int [] qualityProfile = currentStatus.getLowQualityFingers();
	  int [] traumaProfile = currentStatus.getTraumaFingers();
	  
	  printResult("Quality profile", qualityProfile);
	  printResult("Trauma profile", traumaProfile);
	  
	  CompletionStatus completionStatus = currentStatus.enquireFingerStatus(EnquireStatus.TenFingers);
	  printResult("Completion status", completionStatus );
	  
	  
	  // default result for missing fingers excludes the profiles
	  int [] missingFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers);
	  printResult("Missing fingers ", missingFingers);

	  
	  // Explicit result for missing fingers specify exclude low quality 
	  int [] missingFingers2 = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false);

	  // Explicit result for missing fingers specify exclude low quality and trauma
	  int [] missingFingers3 = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false, false);
	  
	  assertContentEqual(missingFingers, missingFingers2);
	  assertContentEqual(missingFingers, missingFingers3);
	  
	  printResult("Missing fingers ", missingFingers);
	  
	  // Missing fingers including low quality
	  int [] missingQFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true);
	  // Equivalent call specifically excluding trauma fingers
	  int [] missingQFingers2 = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true, false);
	  assertContentEqual(missingQFingers, missingQFingers2);
	  
	  printResult("Missing fingers including low q ", missingQFingers);
  
	  int [] missingTFingers = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, true, true);
	  
	  // Note if trauma are included then low quality are automatically included in spite of setting
	  int [] missingTFingers2 = currentStatus.enquireMissingFingers(EnquireStatus.TenFingers, false, true);
	  assertContentEqual(missingTFingers, missingTFingers2);
	  
	  
	  printResult("Missing fingers including trauma ", missingTFingers);

  }
}
