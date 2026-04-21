package com.genkey.partner.workshop;

import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.service.params.EnquireStatus.CompletionStatus;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;

public class IncrementalEnrolScenario extends IncrementalEnrolTests {

  static final String TestSubjectID2 = "2";
  static String TestSubjectID = "1";

  static double FaceQualityThreshold = 1;
  static int MaxMissingFingers = 1;

  public static void main(String[] args) {
    PartnerExample test = new IncrementalEnrolScenario();
    test.processCommandLine(args);
  }

  @Override
  protected void setUp() { // TODO Auto-generated method stub
    super.setUp();
  }

  protected void runAllExamples() {
    enrolFaceFingers();
 //   enrolFingersFace();
  }

  public void enrolFingersFace() {
    this.enrolFingersFace(TestSubjectID2, false);
  }

  public void enrolFaceFingers() {
    enrolFaceFingers(TestSubjectID, false, true);
  }

  /**
   * Story of multiple enrolements based on the following sequence :
   *
   * <ul>
   *   <li>Face
   *   <li>Right hand
   *   <li>Left hand
   *   <li>Thumbs
   * </ul>
   *
   * Shows checks for completion and the use of getEnrolmentHint to guide the operator on next step
   * of enrolment.
   *
   * @param testSubject
   * @param veto
   * @param partialFingerEnrollment
   */
  public void enrolFaceFingers(String testSubject, boolean veto, boolean timeConstrained) {
    // First enrol with face

    int subjectNumber = Integer.valueOf(testSubject);

    GenkeyABISService abisService = ABISServiceModule.getABISService();
    BiographicService biographicService = DGIEServiceModule.getBiographicService();
    
    EnquireStatus status = abisService.enquireSubject(testSubject);
    if (status.existsSubject()) {
      processFlow("Handle existing subject");
      return;
    }
   
    
    BiographicProfileRecord record =
        EnrollmentUtils.getBiographicRecord(TestSubjectID, "John", "Brown");


    SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference(testSubject);
    EnrollmentUtils.enrollFacePortrait(enrollRef, 1);

    biographicService.insertBiographicRecord(record);
    MatchEngineResponse response = abisService.insertIfNoDuplicates(enrollRef, false);
    if (response.hasMatchResults()) {
      // QUERY found results.
      if (!acceptMatchResults(response, veto)) {
        printMessage("Rejecting duplicates detected from query");
        // insert the same enrolment object we created for
        response = abisService.insertSubject(enrollRef);
      } else {
        handleKnownSubject(testSubject, response);
        return;
      }
    }

    // Update with fingers

    // pretend we don't know what is present as this is a new touch point
    EnquireStatus enquireStatus = abisService.enquireSubject(testSubject);

    // check for fingers required

    SubjectEnrollmentReference updateRef = new SubjectEnrollmentReference(testSubject);

    // check for face and enrol if required
    if (!enquireStatus.isFacePresent()) {
      // of course this will not be entered as we have it
      EnrollmentUtils.enrollFacePortrait(updateRef, 1);
    }

    if (enquireStatus.fingerCompletionStatus() != CompletionStatus.Complete) {
      int[] targetFingers;
      if (timeConstrained) {
        targetFingers = enquireStatus.askEnrolmentHint();
      } else {
        targetFingers = enquireStatus.enquireMissingFingers(EnquireStatus.TenFingers);
      }
      updateRef.setTargetFingers(targetFingers);
      EnrollmentUtils.enrollFingerPrintSubject(updateRef, subjectNumber, 1, 1);
    }

    UpdateResponse updateResponse = abisService.updateSubject(updateRef);

    if (updateResponse.hasMatchResults()) {
      PrintMessage("Unexpected duplicates detected on update");
      if (!acceptMatchResults(updateResponse, veto)) {
        printMessage("Rejecting duplicates");
      } else {
        handleLateDuplicate(testSubject, updateResponse);
      }
    }

    // Third visit

    enquireStatus = abisService.enquireSubject(testSubject);
    if (enquireStatus.isFacePresent()
        && enquireStatus.fingerCompletionStatus() == CompletionStatus.Complete) {
      // Nothing more to do
      // exit
      return;
    }

    // Second update
    SubjectEnrollmentReference updateRef3 = new SubjectEnrollmentReference(testSubject);

    //
    if (!enquireStatus.faceRequired(1.0)) {
      EnrollmentUtils.enrollFacePortrait(updateRef3, 2);
    }

    if (enquireStatus.fingerCompletionStatus() != CompletionStatus.Complete) {
      int[] targetFingers;
      if (timeConstrained) {
        // will come back with left hand
        targetFingers = enquireStatus.askEnrolmentHint();
      } else {
        targetFingers = enquireStatus.enquireMissingFingers(EnquireStatus.TenFingers);
      }
      updateRef3.setTargetFingers(targetFingers);
      EnrollmentUtils.enrollFingerPrintSubject(updateRef3, subjectNumber, 1, 1);
    }

    UpdateResponse updateResponse2 = abisService.updateSubject(updateRef3);

    if (updateResponse.hasMatchResults()) {
      PrintMessage("Unexpected duplicates detected on update");
      if (acceptMatchResults(updateResponse, veto)) {
        printMessage("Rejecting duplicates");
      } else {
        handleLateDuplicate(testSubject, updateResponse);
      }
    }

    // fourth visit.

    enquireStatus = abisService.enquireSubject(testSubject);
    if (enquireStatus.enrollmentComplete(MaxMissingFingers, FaceQualityThreshold)) {
      // although we have 2 missing fingers it is not complete because
      // thumbs have none present
      return;
    }

    // Third update
    SubjectEnrollmentReference updateRef4 = new SubjectEnrollmentReference(testSubject);

    if (enquireStatus.faceRequired(1.0)) {
      EnrollmentUtils.enrollFacePortrait(updateRef4, 2);
    }

    if (!enquireStatus.fingerEnrollmentComplete(MaxMissingFingers)) {
      // will ask for thumbs
      int[] fingers = enquireStatus.askEnrolmentHint();
      updateRef4.setTargetFingers(fingers);
      EnrollmentUtils.enrollFingerPrintSubject(updateRef4, subjectNumber, 1, 1);
    }

    UpdateResponse updateResponse3 = abisService.updateSubject(updateRef);
    if (updateResponse.hasMatchResults()) {
      PrintMessage("Unexpected duplicates detected on update");
      if (acceptMatchResults(updateResponse, veto)) {
        printMessage("Rejecting duplicates");
      } else {
        handleLateDuplicate(testSubject, updateResponse);
      }
    }
  }

  public void enrolFingersFace(String testSubject, boolean veto) {
    int subjectNumber = Integer.valueOf(testSubject);

    GenkeyABISService abisService = ABISServiceModule.getABISService();
    BiographicService biographicService = DGIEServiceModule.getBiographicService();
    BiographicProfileRecord record =
        EnrollmentUtils.getBiographicRecord(TestSubjectID, "John2", "Brown2");

    EnquireStatus status = abisService.enquireSubject(testSubject);
    if (status.existsSubject()) {
      processFlow("Handle existing subject");
      return;
    }

    SubjectEnrollmentReference enrollRef = new SubjectEnrollmentReference(testSubject);
    biographicService.insertBiographicRecord(record);
    // Time is short right hand only
    enrollRef.setTargetFingers(RightHandFingers);
    EnrollmentUtils.enrollFingerPrintSubject(enrollRef, subjectNumber, 1, 1);

    MatchEngineResponse response = abisService.insertIfNoDuplicates(enrollRef, false);
    if (response.hasMatchResults()) {
      // QUERY found results.
      if (!acceptMatchResults(response, veto)) {
        printMessage("Rejecting duplicates detected from query");
        // insert the same enrolment object we created for
        response = abisService.insertSubject(enrollRef);
      } else {
        handleKnownSubject(testSubject, response);
        return;
      }
    }

    // Update with face

    // pretend we don't know what is present as this is a new touch point
    EnquireStatus enquireStatus = abisService.enquireSubject(testSubject);

    SubjectEnrollmentReference updateRef = new SubjectEnrollmentReference(testSubject);

    // check for face and enrol if required
    if (!enquireStatus.isFacePresent()) {
      // of course this will not be entered as we have it
      EnrollmentUtils.enrollFacePortrait(updateRef, 1);
    }
    // time is short - settle for face
    UpdateResponse updateResponse = abisService.updateSubject(enrollRef);

    if (updateResponse.hasMatchResults()) {
      // etc
    }

    // Next visit
    // Finish update for remaining fingers
    enquireStatus = abisService.enquireSubject(testSubject);

    if (enquireStatus.enrollmentComplete(0, FaceQualityThreshold)) {
      // check for completion
      return;
      // EnrollmentUtils.enrollFacePortrait(updateRef, 1);

    }

    SubjectEnrollmentReference updateRef2 = new SubjectEnrollmentReference(testSubject);
    if (enquireStatus.faceRequired(FaceQualityThreshold)) {
      EnrollmentUtils.enrollFacePortrait(updateRef, 2);
    }

    // We have time ..
    if (enquireStatus.fingerEnrollmentComplete(0)) {
      // find all fingers required and enroll all of them
      int[] fingers = enquireStatus.enquireMissingFingers(TenFingers);
      updateRef.setTargetFingers(fingers);
      EnrollmentUtils.enrollFingerPrintSubject(updateRef2, subjectNumber, 1, 1);
    }
    // Perform the update
    updateResponse = abisService.updateSubject(enrollRef);
    if (updateResponse.hasMatchResults()) {
      // etc
    }
  }
}
