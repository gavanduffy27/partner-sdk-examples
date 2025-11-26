package com.genkey.abisclient.examples.bordercontrol;

import com.genkey.abisclient.Enums.GKSwitchParameter;
import com.genkey.abisclient.Enums.GKXmlSchema;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.ABISOperation;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.imageio.ImageIO;

public class PassportExample extends BorderControlExample {

  static String ThumbsImage = "Thumbs_01";
  static String V1Left = "v1-left";
  static String V1Right = "v1-right";

  static String Kojak = "kojak_image2";
  static String SERGE = "serge_image2";

  static int LeftHandFingers[] = {10, 9, 8, 7};
  static int rightHandFingers[] = {2, 3, 4, 5};
  static int thumbs[] = {1, 6};
  static int TenFingers[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

  @Override
  protected void runAllExamples() {
    testSergeSegment();
    // testImageSegment();
    //		testPassengerEnrol();
  }

  public void testImageSegment() {
    String baseName = Kojak;
    ImageData leftHandImage = getFourFingerImage(baseName);

    SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference();
    subjectReference.setSubjectID("subject1");
    ImageContext context = new ImageContext(leftHandImage, LeftHandFingers);
    int[] fingers = context.getFingers();
    int nImages = context.count();
    boolean autoRotate = true;
    for (int ix = 0; ix < nImages; ix++) {
      ImageData fingerSegment = context.extractImageSegment(ix, autoRotate);
      byte[] imageData = fingerSegment.asEncodedImage(ImageData.FORMAT_BMP);
      int fingerId = fingers[ix];

      String testFile = super.getTestFile(baseName + "_" + ix, "bmp");
      try {
        FileUtils.byteArrayToFile(imageData, testFile);
      } catch (Exception e) {
        handleException(e);
      }
      ReferenceDataItem referenceData = context.getReferenceData(ix);
      FingerEnrollmentReference ref = new FingerEnrollmentReference(fingerId, false);
      ref.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
      subjectReference.add(ref);
    }
    String xmlContent = subjectReference.toXml(GKXmlSchema.ABIS4);
    String xmlFile = super.getTestFile(baseName, "xml");
    try {
      FileUtils.stringToFile(xmlContent, xmlFile);
    } catch (Exception e) {
      handleException(e);
    }
  }

  public void testSergeSegment() {
    String baseName = SERGE;
    ImageData leftHandImage = getFourFingerImage(baseName);

    SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference();
    subjectReference.setSubjectID("subject1");
    ImageContext context = new ImageContext(leftHandImage, LeftHandFingers);
    int[] fingers = context.getFingers();
    int nImages = context.count();
    boolean autoRotate = true;
    for (int ix = 0; ix < nImages; ix++) {
      ImageData fingerImageData = context.extractImageSegment(ix, autoRotate);
      int fingerId = fingers[ix];

      String testFile = super.getTestFile(baseName + "_" + ix, "bmp");
      // writeImage(fingerImageData, "FINGER_" + fingerId + ".BMP");
      writeImage2(fingerImageData, testFile);
      ReferenceDataItem referenceData = context.getReferenceData(ix);

      FingerEnrollmentReference fingerReference = new FingerEnrollmentReference(fingerId, false);
      fingerReference.addImageData(fingerImageData, ImageData.FORMAT_WSQ);
      fingerReference.addReferenceData(referenceData);
      subjectReference.add(fingerReference);
    }
    String xmlContent = subjectReference.toXml(GKXmlSchema.ABIS4);
    String xmlFile = super.getTestFile(baseName, "xml");
    try {
      FileUtils.stringToFile(xmlContent, xmlFile);
    } catch (Exception e) {
      handleException(e);
    }
  }

  public void writeImage2(final ImageData imageData, String fileName) {
    try {
      BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageData.asEncodedImage("BMP")));
      ImageIO.write(bi, "bmp", new File(fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(final ImageData imageData, String fileName) {
    try {
      byte[] image = imageData.asEncodedImage("BMP");
      File outputFile = new File(fileName);
      Files.write(outputFile.toPath(), image);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void testPassengerEnrol() {

    // Create subject container object for enrollment
    SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference();
    subjectReference.setTargetFingers(TenFingers);
    subjectReference.setTargetSampleCount(1);

    // Capture left hand
    boolean autoExtract = false;
    ImageData leftHandImage = getFourFingerImage(Kojak);
    ImageContext context = new ImageContext(leftHandImage, LeftHandFingers);

    int numSegments = context.count();
    int[] fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      ReferenceDataItem referenceData = context.getReferenceData(index);
      FingerEnrollmentReference fingerReference = new FingerEnrollmentReference(finger, false);
      double qScore = referenceData.getQualityScore();
      if (qScore > 12) {
        fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
        fingerReference.addReferenceData(referenceData);
        subjectReference.add(fingerReference);
      }
    }

    // Review status
    showEnrollmentStatus(subjectReference);

    // Capture Right hand
    autoExtract = true;
    subjectReference.setQualityThreshold(12);

    ImageData rightHandImage = getFourFingerImage(V1Right);

    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, false);
    context = new ImageContext(leftHandImage, RightHandFingers);

    numSegments = context.count();
    fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      FingerEnrollmentReference fingerReference =
          new FingerEnrollmentReference(finger, autoExtract);
      fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
      subjectReference.addChecked(fingerReference);
    }

    // Review status
    showEnrollmentStatus(subjectReference);

    // Capture thumbs
    ImageData thumbImage = getFourFingerImage(ThumbsImage);
    context = new ImageContext(thumbImage, Thumbs);
    fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      FingerEnrollmentReference fingerReference =
          new FingerEnrollmentReference(finger, autoExtract);
      fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
      subjectReference.addChecked(fingerReference);
    }

    // Review status
    showEnrollmentStatus(subjectReference);

    String xmlRequest = subjectReference.toXml(GKXmlSchema.ABIS4, ABISOperation.Insert);

    // Process the XML request to ABIS
    processWithRest(xmlRequest);

    // Process with SDK Rest wrapper ..  (

    GenkeyABISService service = getABISService();

    MatchEngineResponse response = service.insertSubject(subjectReference, false);
    int status = response.getStatusCode();
    List<MatchResult> results = response.getMatchResults();
  }

  public void testSubjectVerify() {
    String passportId = "userId";
    SubjectEnrollmentReference reference = enrolSubjectReference(passportId);
    String xmlVerify = reference.toXml(GKXmlSchema.ABIS4, ABISOperation.Verify);
  }

  public SubjectEnrollmentReference enrolSubjectReference(String subjectId) {
    // Create subject container object for enrollment
    SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference();
    subjectReference.setSubjectID(subjectId);
    subjectReference.setTargetFingers(TenFingers);
    subjectReference.setTargetSampleCount(1);

    // Capture left hand
    boolean autoExtract = false;
    ImageData leftHandImage = getFourFingerImage(V1Left);
    ImageContext context = new ImageContext(leftHandImage, LeftHandFingers);

    int numSegments = context.count();
    int[] fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      ReferenceDataItem referenceData = context.getReferenceData(index);
      FingerEnrollmentReference fingerReference = new FingerEnrollmentReference(finger, false);
      double qScore = referenceData.getQualityScore();
      if (qScore > 12) {
        fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
        fingerReference.addReferenceData(referenceData);
        subjectReference.add(fingerReference);
      }
    }

    // Review status
    showEnrollmentStatus(subjectReference);

    // Capture Right hand
    autoExtract = true;
    subjectReference.setQualityThreshold(12);

    ImageData rightHandImage = getFourFingerImage(V1Right);

    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, false);
    context = new ImageContext(leftHandImage, RightHandFingers);

    numSegments = context.count();
    fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      FingerEnrollmentReference fingerReference =
          new FingerEnrollmentReference(finger, autoExtract);
      fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
      subjectReference.addChecked(fingerReference);
    }

    // Review status
    showEnrollmentStatus(subjectReference);

    // Capture thumbs
    ImageData thumbImage = getFourFingerImage(ThumbsImage);
    context = new ImageContext(thumbImage, Thumbs);
    fingers = context.getFingers();
    for (int index = 0; index < numSegments; index++) {
      int finger = fingers[index];
      ImageData fingerSegment = context.extractImageSegment(index, autoExtract);
      FingerEnrollmentReference fingerReference =
          new FingerEnrollmentReference(finger, autoExtract);
      fingerReference.addImageData(fingerSegment, ImageData.FORMAT_WSQ);
      subjectReference.addChecked(fingerReference);
    }
    return subjectReference;
  }

  public void processWithRest(String xmlRequest) {
    // Application defined REST wrapper to Access ABIS

  }

  private void showEnrollmentStatus(SubjectEnrollmentReference subjectReference) {
    int[] fingersComplete = subjectReference.getFingersComplete();
    int[] fingersRequired = subjectReference.getFingersIncomplete();
    printResult("Fingers Complete", fingersComplete);
    printResult("Fingers Required", fingersRequired);
    printObject("Enrollment status", subjectReference.printReferenceState());
  }

  static GenkeyABISService getABISService() {
    /*
    RestABISService abisService = new RestABISService();
    abisService.setHost("AbisTestHost");
    abisService.setPort(8000);
    */
    return null;
  }

  public static ImageData getFourFingerImage(String baseName) {
    String fileName = TestDataManager.getImageFile(baseName, TestDataManager.FourFingerPath);
    return TestDataManager.loadImage(fileName);
  }
}
