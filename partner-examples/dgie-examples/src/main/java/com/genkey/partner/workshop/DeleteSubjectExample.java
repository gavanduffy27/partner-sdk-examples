package com.genkey.partner.workshop;

import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.params.EnquireStatus;

public class DeleteSubjectExample extends BMSWorkshopExample {

  public static void main(String[] args) {
    DeleteSubjectExample test = new DeleteSubjectExample();
    test.processCommandLine(args);
  }

  @Override
  protected void runAllExamples() {
    deleteSubjectTest();
  }

  public void deleteSubjectTest() {
    deleteSubjectTest(TestSubjectID2);
  }

  private void deleteSubjectTest(String subjectID) {
    GenkeyABISService abisService = this.getAbisService();

    if (!abisService.testAvailable()) {
      return;
    }

    // status before enrolment
    EnquireStatus currentStatus = abisService.enquireSubject(subjectID);
    if (! currentStatus.existsSubject()) {
    	printErrorF("Test not supported for non existent subject %s ", subjectID);
    	return;
    }
    
    boolean status = abisService.deleteSubject(subjectID, true);
    printResult("Status on delete", status);
    currentStatus = abisService.enquireSubject(subjectID);
    boolean existsDeleted = currentStatus.existsSubject();
    printResult("Status post delete", existsDeleted);
    
  }
}
