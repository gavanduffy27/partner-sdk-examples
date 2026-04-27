package com.genkey.partner.workshop;

import com.genkey.partner.example.PartnerExample;

public class IncrementalEnroll4Steps extends IncrementalEnrolTests {

  static String TestSubjectId = "1";

  static boolean faceFirst = true;

  static boolean enforceQuality = true;

  public static void main(String[] args) {
    PartnerExample test = new IncrementalEnroll4Steps(true);
    test.processCommandLine(args);
  }
  
  
  public IncrementalEnroll4Steps() {
	  
  }

  public IncrementalEnroll4Steps(boolean useBiographic) {
	  super(useBiographic);
  }
  
  
  @Override
  protected void runAllExamples() {
    initialEnrollFace();
    simple4StepEnroll();
    initialEnrollFinger();
    nextEnrollStep();
  }

  public void simple4StepEnroll() {
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.Face);
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.RightHand);
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.LeftHand);
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.Complete);
  }

  public void performNextTest() {
    //	this.performEnrolNextStep(TestSubjectId, EnrollmentStep.Face);
    //    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.RightHand);
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.LeftHand);
    this.performEnrolNextStep(TestSubjectId, EnrollmentStep.Complete);
  }

  public void nextEnrollStep() {
    checkEnrolNextStep();
  }

  private boolean checkEnrolNextStep() {
    return super.checkEnrolNextStep(TestSubjectId, faceFirst, enforceQuality, true);
  }

  public void initialEnrollFinger() {
    checkEnrolNextStep(TestSubjectId, false, false, true);
  }

  public void initialEnrollFace() {
    checkEnrolNextStep(TestSubjectId, true, false, true);
  }

  // Full enrolment control loop for synthetic multiple visits
  public void incrementalEnrollLoop() {
    boolean status = false;
    while (!status) {
      status = checkEnrolNextStep();
    }
  }
}
