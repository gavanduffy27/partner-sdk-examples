package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageContextSDK.MatchDetails;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.transport.FingerEnrollmentReference;

public class AuthenticationExample extends TransportExampleModule {

  @Override
  protected void runAllExamples() {
    runAuthenticationExample();
  }

  public void runAuthenticationExample() {
    testAuthentication(1, 2, 3);
  }

  void testAuthentication(int subject1, int subject2, int finger) {
    // Calls the helper routine that Creates the PK for given combination of subject, finger and
    // sample index
    // This one is the one we are foing to compare
    FingerEnrollmentReference enrolRef = generateFingerBlob(subject1, finger, 1, false, true);

    for (int sampleIndex = 1; sampleIndex <= 2; sampleIndex++) {
      // Get a genuine and impostor for sample1 and sample 2
      FingerEnrollmentReference genuine =
          generateFingerBlob(subject1, finger, sampleIndex, false, true);
      FingerEnrollmentReference impostor =
          generateFingerBlob(subject2, finger, sampleIndex, false, true);
      testMatchReferences(enrolRef, genuine, true);
      testMatchReferences(enrolRef, impostor, false);
    }
  }

  // Performs the PK match
  void testMatchReferences(
      FingerEnrollmentReference enrolment, FingerEnrollmentReference test, Boolean expected) {
    // Threshold of 60 means FAR of 1E-6 .. 1 in a million chance for an impostor to match
    double threshold = 60;
    // Access the references
    ReferenceDataItem enrolRef = enrolment.getReferenceDataItem();
    ReferenceDataItem testRef = test.getReferenceDataItem();
    ImageContextSDK.MatchDetails matchDetails = new MatchDetails();

    // This is the SDK function used to calulate the score .. it is not yet in the format 0 to 1
    double score = ImageContextSDK.matchReferences(enrolRef, testRef);

    // Provides a probability on a logarithmic scale that this is a genuine match
    double probScore = ImageContextSDK.mapLog10ScoreToProbability((int) score);

    boolean isDup = (score >= threshold);

    // The real score is formed as -10 * Log10(Far) and is much more useful than the dumb
    // probability score

    // isDup is true if it is a duplicate
    if (expected) {
      if (isDup) {
        PrintMessage("Expected match with score / prob " + score + "/" + probScore);
      } else {
        PrintMessage("Unexpected match failure with score/ prob " + score + "/" + probScore);
      }
    } else {
      if (isDup) {
        PrintMessage("Unexpected match with score / prob " + score + "/" + probScore);
      } else {
        PrintMessage("Expected match failure with score / prob " + score + "/" + probScore);
      }
    }
  }
}
