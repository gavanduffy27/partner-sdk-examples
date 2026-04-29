package com.genkey.partner.workshop;

import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicProfileRecord.Gender;
import com.genkey.partner.utils.EnrollmentUtils;

public class IncrementalEnrolTests extends BMSWorkshopExample {

  static final String TestSubjectID2 = "2";
  static String TestSubjectID = "1";

  boolean enrollBiographics = false;

  // Guidance update on face-quality to be addressed later
  static double FaceQualityThreshold = 0.7;

  // This is a tolerance setting but in reality it is an application process decision as to
  // how many times we go back to the well. ABIS does not record which fingers have been attempted
  // only those that have been successful.
  static int MaxMissingFingers = 1;

  public static enum EnrollmentStep {
    Face,
    LeftHand,
    RightHand,
    Thumbs,
    EnrollmentHint,
    Complete
  }

  public IncrementalEnrolTests() {
    this(true);
  }

  public IncrementalEnrolTests(boolean useBiographicOnQuery) {
    this.setEnrollBiographics(useBiographicOnQuery);
  }

  /**
   * Smart enrolment function that applies a strategy for determining what biometric data to ask for
   * purposes of enrolment. The assumption is that if this returns true then we proceed to VERIFY.
   *
   * <p>Note this is intended only as an example of how to use the SDK to implement a strategy
   *
   * <p>Note this will exit with status true immediately if enrolment is already complete.
   *
   * <p>If time-constrained
   *
   * @param subjectId Subject to be checked - for enroll or update
   * @param faceFirst If true then ensure face is enrolled before fingers
   * @param enforceFaceQuality If true then ask for face if below quality threshold
   * @param timeConstrained If true then just perform an incremental step
   * @return
   */
  protected boolean checkEnrolNextStep(
      String subjectId, boolean faceFirst, boolean enforceFaceQuality, boolean timeConstrained) {

    GenkeyABISService abisService = this.getAbisService();

    if (!abisService.testAvailable()) {
      return false;
    }

    boolean exists = abisService.existsSubject(subjectId);

    boolean isInsert = !exists;

    EnquireStatus status = null;
    if (exists) {
      status = abisService.enquireSubject(subjectId);

      if (status.enrollmentComplete(MaxMissingFingers, FaceQualityThreshold)) {
        // Exit with enrolment complete
        return true;
      }
    }
    SubjectEnrollmentReference subjectRef = new SubjectEnrollmentReference(subjectId);

    boolean faceNext;
    boolean captureFace;
    boolean captureFingers;

    if (!timeConstrained) {
      captureFace = true;
      captureFingers = true;
    } else {
      if (isInsert) {
        faceNext = faceFirst;
      } else {
        if (enforceFaceQuality) {
          faceNext = status.faceRequired(FaceQualityThreshold);
        } else {
          faceNext = !status.isFaceTemplatePresent();
        }
      }
      captureFace = faceNext;
      captureFingers = !faceNext;
    }

    if (captureFace) {
      EnrollmentUtils.enrollFacePortrait(subjectRef, 1);
    }
    if (captureFingers) {
      int[] targetFingers;

      if (isInsert) {
        if (timeConstrained) {
          targetFingers = EnquireStatus.RightHand;
        } else {
          targetFingers = EnquireStatus.TenFingers;
        }
      } else {
        if (timeConstrained) {
          targetFingers = status.askEnrolmentHint();
        } else {
          targetFingers = status.enquireMissingFingers(TenFingers);
        }
      }
      subjectRef.setTargetFingers(targetFingers);
      EnrollmentUtils.enrollFingerPrintSubject(subjectRef, Integer.parseInt(subjectId), 1, 1);
    }
    if (this.isEnrollBiographics() && exists) {
      BiographicProfileRecord profile =
          EnrollmentUtils.getSimpleBiographicRecord(null, "john", "doe", Gender.Male);
      subjectRef.setBiographicData(profile.getBiographicData());
    }

    MatchEngineResponse response;
    if (isInsert) {
      response = abisService.insertIfNoDuplicates(subjectRef);
      if (response.hasMatchResults()) {
        super.handleKnownSubject(subjectId, response);
        return true;
      }
    } else {
      response = abisService.updateSubject(subjectRef);
      if (response.hasMatchResults()) {
        super.handleLateDuplicate(subjectId, (UpdateResponse) response);
      }
    }

    // Check for completeness
    status = abisService.enquireSubject(subjectId);
    return status.enrollmentComplete(MaxMissingFingers, FaceQualityThreshold);
  }

  protected boolean performEnrolNextStep(String subjectId, EnrollmentStep step) {
    GenkeyABISService abisService = this.getAbisService();

    if (!abisService.testAvailable()) {
      return false;
    }

    EnquireStatus enquireStatus = abisService.enquireSubject(subjectId);

    if (enquireStatus.enrollmentComplete(MaxMissingFingers, FaceQualityThreshold)) {
      // Exit with enrolment complete
      return true;
    }

    boolean exists = enquireStatus.existsSubject();

    boolean isInsert = !exists;

    SubjectEnrollmentReference enrolRef  = this.acquireBiometrics(subjectId, step, enquireStatus, this.isEnrollBiographics());
    
    /*
    SubjectEnrollmentReference enrolRef = new SubjectEnrollmentReference(subjectId);

    if (step == EnrollmentStep.Face) {
      EnrollmentUtils.enrollFacePortrait(enrolRef, 1);
    } else {
      int[] targetFingers = selectTargetFingers(enquireStatus, step);
      enrolRef.setTargetFingers(targetFingers);
      int subjectNumber = Integer.valueOf(subjectId);
      EnrollmentUtils.enrollFingerPrintSubject(enrolRef, subjectNumber, 1, 1);
    }
    */

    MatchEngineResponse response;
    if (isInsert) {
      response = abisService.insertIfNoDuplicates(enrolRef);
      if (response.hasMatchResults()) {
        if (super.acceptMatchResults(response, false)) {
          return true;
        }
      }
    } else {
      response = abisService.updateSubject(enrolRef);
      if (response.hasMatchResults()) {
        super.handleLateDuplicate(subjectId, response);
      }
    }

    enquireStatus = abisService.enquireSubject(subjectId);
    return enquireStatus.enrollmentComplete(MaxMissingFingers, FaceQualityThreshold);
  }

  protected SubjectEnrollmentReference acquireBiometrics(String subjectId, EnrollmentStep step) {
	  return acquireBiometrics(subjectId, step, EnquireStatus.UnknownStatus);
  }
  
  protected SubjectEnrollmentReference acquireBiometrics(String subjectId, EnrollmentStep step, EnquireStatus status) {
	  return acquireBiometrics(subjectId, step, status, this.isEnrollBiographics());
  }
  
  protected SubjectEnrollmentReference acquireBiometrics(String subjectId, EnrollmentStep step, EnquireStatus status, boolean withBiographics) {
	  SubjectEnrollmentReference result = new SubjectEnrollmentReference(subjectId);
	  int subjectNumber = Integer.valueOf(subjectId);
	  acquireBiometrics(result, subjectNumber, step, status, withBiographics);	  
	  return result;
  }
  
  protected void acquireBiometrics(
      SubjectEnrollmentReference enrolRef,
      int subjectNumber,
      EnrollmentStep step,
      EnquireStatus enquireStatus, boolean withBiographics) {
    if (step == EnrollmentStep.Face) {
      EnrollmentUtils.enrollFacePortrait(enrolRef, 1);
    } else {
      int[] targetFingers = selectTargetFingers(enquireStatus, step);
      enrolRef.setTargetFingers(targetFingers);
      EnrollmentUtils.enrollFingerPrintSubject(enrolRef, subjectNumber, 1, 1);
    }
    if (withBiographics) {
    	EnrollmentUtils.enrollBiographics(enrolRef, TestSubjectID);
    }
  }

  public MatchEngineResponse performQuery(String subjectId, EnrollmentStep step, boolean withBiographics) {
	  SubjectEnrollmentReference enrollRef = acquireBiometrics(subjectId, step, EnquireStatus.UnknownStatus, true);
	  
	    GenkeyABISService abisService = this.getAbisService();

	    if (!abisService.testAvailable()) {
	      return null;
	    }
	  
	   MatchEngineResponse response = abisService.querySubject(enrollRef);	   	   
	   return response;	  
  }

  public MatchEngineResponse insertIfNoDuplicates(String subjectId, EnrollmentStep step, boolean withBiographics) {
	  SubjectEnrollmentReference enrollRef = acquireBiometrics(subjectId, step, EnquireStatus.UnknownStatus, true);
	  
	    GenkeyABISService abisService = this.getAbisService();

	    if (!abisService.testAvailable()) {
	      return null;
	    }
	  
	   MatchEngineResponse response = abisService.insertIfNoDuplicates(enrollRef);	   	   
	   return response;	  
  }
  
  
  private int[] selectTargetFingers(EnquireStatus enquireStatus, EnrollmentStep step) {
    int[] targetFingers;
    if (enquireStatus == null) {
      enquireStatus = EnquireStatus.UnknownStatus;
    }
    switch (step) {
      case LeftHand:
        targetFingers = enquireStatus.enquireMissingFingers(EnquireStatus.LeftHand);
        break;
      case RightHand:
        targetFingers = EnquireStatus.RightHand;
        break;
      case Thumbs:
        targetFingers = EnquireStatus.Thumbs;
        break;

      case EnrollmentHint:
        targetFingers = enquireStatus.askEnrolmentHint();
        break;
      case Complete:
      default:
        targetFingers = enquireStatus.enquireMissingFingers(EnquireStatus.TenFingers);
        break;
    }
    return targetFingers;
  }

  private void enrollFace(
      SubjectEnrollmentReference enrolRef, String subjectId) { // TODO Auto-generated method stub
  }

  protected boolean isEnrollBiographics() {
    return enrollBiographics;
  }

  protected void setEnrollBiographics(boolean useBiographicOnQuery) {
    this.enrollBiographics = useBiographicOnQuery;
  }
}
