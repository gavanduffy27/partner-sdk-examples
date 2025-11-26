package com.genkey.partner.example.functional;

import com.genkey.abisclient.Enums.GKXmlSchema;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.example.PartnerTestSuite;
import com.genkey.platform.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SegmentationExample extends PartnerExample {

  // final static String TestImage="serge_image2";
  static final String TestImage = "v1-left";

  static String argumentFile = null;

  public static void main(String[] args) {
    if (args.length == 1) {
      argumentFile = args[0];
    }
    SegmentationExample example = new SegmentationExample();
    example.runTestExamples();
  }

  @Override
  protected void setUp() {
    PartnerTestSuite.initClient();
    String cfgPath = ImageContextSDK.getConfigurationPath();
    TestDataManager.setFingerImagePath(4);
    String imagePath = TestDataManager.getImageDirectory();
    printResult("Config path", cfgPath);
    printResult("Image path", imagePath);
  }

  @Override
  protected void runAllExamples() {
    // testSegmentation();
    testSegmentation2();
  }

  public void testSegmentation() {
    testSegmentation(TestImage, PartnerExample.LeftHand);
  }

  public void testSegmentation(String imageName, int fingers[]) {
    String imageFile = TestDataManager.getImageFile(imageName);
    ImageData imageData = TestDataManager.loadImage(imageFile);
    testSegmentation(imageName, imageData, fingers);
  }

  public void testSegmentation(String imageName, ImageData imageData, int[] fingers) {

    String format = ImageData.FORMAT_BMP;
    ImageContext context = new ImageContext(imageData, fingers);
    int nFingers = context.count();
    SubjectEnrollmentReference subjectRef = new SubjectEnrollmentReference();
    subjectRef.setSubjectID("subject_" + imageName);
    for (int ix = 0; ix < nFingers; ix++) {
      int fingerId = fingers[ix];
      String baseName = imageName + "_" + fingerId;
      String testFile = this.getTestFile(baseName, format, false);
      ImageData segment = context.extractImageSegment(ix, true);
      ReferenceDataItem reference = context.getReferenceData(ix);

      FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(fingerId, false);
      fingerRef.addImageData(segment, ImageData.FORMAT_BMP);
      fingerRef.addReferenceData(reference);

      subjectRef.add(fingerRef);

      byte[] encoding = segment.asEncodedImage(format);
      try {
        println("Exporting segment " + fingerId + " to " + testFile);
        FileUtils.byteArrayToFile(encoding, testFile);
      } catch (Exception e) {
        handleException(e);
      }
    }

    String xmlFile = this.getTestFile(imageName, FileUtils.EXT_XML);
    String xmlContent = subjectRef.toXml(GKXmlSchema.ABIS4);
    try {
      println("Exporting XML request for " + imageName + " to " + xmlFile);
      FileUtils.stringToFile(xmlContent, xmlFile);
    } catch (Exception e) {
      handleException(e);
    }
  }

  public void testSegmentation2() {
    if (argumentFile == null) {
      argumentFile = TestDataManager.getImageFile(TestImage);
    }
    testSegmentation2(argumentFile, PartnerExample.LeftHand, true);
  }

  public void testSegmentation2(String fileName, int[] fingers, boolean useDetected) {
    // String imageName = "kojak_image.bmp";
    ImageData imageData = null;
    String imageName = FileUtils.baseName(fileName);
    try {
      imageData = new ImageData(Files.readAllBytes(new File(fileName).toPath()), "bmp");
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    String format = ImageData.FORMAT_BMP;

    ImageContext context = new ImageContext(imageData, fingers);
    if (useDetected) {
      fingers = context.getDetectedFingers();
    }

    int nFingers = context.count();
    SubjectEnrollmentReference subjectRef = new SubjectEnrollmentReference();
    subjectRef.setSubjectID("subject_" + imageName);
    for (int ix = 0; ix < nFingers; ix++) {
      int fingerId = fingers[ix];
      String baseName = imageName + "_" + fingerId;
      String testFile = "TEST_" + fingerId + ".BMP";
      ImageData segment = context.extractImageSegment(ix, true);
      ReferenceDataItem reference = context.getReferenceData(ix);

      FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(fingerId, false);
      fingerRef.addImageData(segment, ImageData.FORMAT_BMP);
      fingerRef.addReferenceData(reference);

      subjectRef.add(fingerRef);

      byte[] encoding = segment.asEncodedImage(format);
      try {
        println("Exporting segment " + fingerId + " to " + testFile);
        FileUtils.byteArrayToFile(encoding, testFile);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    String xmlFile = "TEST_REQUEST.XML";
    String xmlContent = subjectRef.toXml(GKXmlSchema.ABIS4);
    try {
      println("Exporting XML request for " + imageName + " to " + xmlFile);
      FileUtils.stringToFile(xmlContent, xmlFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
