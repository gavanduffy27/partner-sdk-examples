package com.genkey.partner.workshop;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.platform.utils.FileUtils;

public class ImageCaptureExample extends BMSWorkshopExample {

  private static final String TestSubject = "1";

  public static void main(String[] args) {
    PartnerExample test = new ImageCaptureExample();
    test.processCommandLine(args);
  }

  public void setUp() {
    super.setUp();
    String cfgPath = ImageContextSDK.getConfigurationPath();
    TestDataManager.setFingerImagePath(4);
    String imagePath = TestDataManager.getImageDirectory();
    printResult("Config path", cfgPath);
    printResult("Image path", imagePath);
  }

  @Override
  public void runAllExamples() {
    captureComplexFlow();
  }

  public void captureComplexFlow() {

    GenkeyABISService abisService = ABISServiceModule.getABISService();

    TestDataManager.setFingerImagePath(4);
    SubjectEnrollmentReference reference = new SubjectEnrollmentReference(TestSubject);
    String imageFile = TestDataManager.getImageFile(PartnerExample.V1Left);
    ImageBlob imageData = TestDataManager.loadImageBlob(imageFile);
    byte[] encoding = imageData.getImageEncoding();
    String format = imageData.getImageFormat();
    EnrollmentUtils.addSegmentationImage(reference, encoding, format, LeftHandFingers, true);

    MatchEngineResponse response = abisService.querySubject(reference);

    // Assume verification failure

    ImageBlob imagePortrait = TestDataManager.getSubjectPortraitBlob(TestSubject, 1);
    reference.setFacePortrait(imagePortrait);

    MatchEngineResponse response2 = abisService.querySubject(reference);

    // Assume verification failure
    TestDataManager.setFingerImagePath(4);
    String imageFile2 = TestDataManager.getImageFile(PartnerExample.V1Right);
    ImageBlob imageData2 = TestDataManager.loadImageBlob(imageFile2);
    byte[] encoding2 = imageData2.getImageEncoding();
    String format2 = imageData2.getImageFormat();
    EnrollmentUtils.addSegmentationImage(reference, encoding2, format2, RightHandFingers, true);

    // Perform ABIS operation
    MatchEngineResponse response3 = abisService.querySubject(reference);

    // Complete the enrolment
    TestDataManager.setFingerImagePath(2);
    String imageFile3 = TestDataManager.getImageFile(PartnerExample.Thumbs01);
    ImageBlob imageData3 = TestDataManager.loadImageBlob(imageFile3);
    byte[] encoding3 = imageData3.getImageEncoding();
    String format3 = imageData3.getImageFormat();
    EnrollmentUtils.addSegmentationImage(reference, encoding3, format3, Thumbs, true);

    // Perform ABIS operation
    MatchEngineResponse response4 = abisService.querySubject(reference);

    // Check for sufficiency
    reference.setQualityThreshold(20);
    reference.setTargetFingers(TenFingers);

    TestDataManager.setFingerImagePath(1);
    int sampleIndex = 1;
    int maxPresentations = 3;
    int subjectId = Integer.valueOf(TestSubject);

    // Check for completeness
    boolean isComplete = reference.isComplete();

    if (!isComplete) {
      while (!reference.isComplete() && sampleIndex < maxPresentations) {
        int[] fingers = reference.getFingersIncomplete();

        for (int finger : fingers) {
          ImageData image = TestDataManager.loadImage(subjectId, finger, maxPresentations);
          FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(finger, true);
          fingerRef.addImageData(image, ImageData.FORMAT_WSQ);
          reference.add(fingerRef);
        }
        sampleIndex++;
      }
    }
    
    // Check if subject exists 
    MatchEngineResponse queryResponse = abisService.querySubject(reference);
    if (! queryResponse.hasMatchResults())  {
    	MatchEngineResponse insertResponse = abisService.querySubject(reference);
    }
    
  }
}
