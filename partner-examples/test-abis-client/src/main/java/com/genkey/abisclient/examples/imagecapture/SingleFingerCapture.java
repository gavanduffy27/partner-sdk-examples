package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.Enums.GKSwitchParameter;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.Enums.GKXmlSchema;
import com.genkey.abisclient.ErrorStatusCodes;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.QualityInfo.QualityRank;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.SegmentationRegion;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.StopWatch;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.ImageUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SingleFingerCapture extends ExampleModule {

  public static void main(String[] args) {
    SingleFingerCapture test = new SingleFingerCapture();
    test.runTestExamples();
  }

  @Override
  protected void setUp() {
    super.setUp();
    TestDataManager.setImageDirectory(ExampleModule.SingleFingerDirectory);
    TestDataManager.setImageFormat(ImageUtils.FORMAT_BMP);
  }

  @Override
  protected void runAllExamples() {
    //		doNullTest();
    imageRotateTest();
    simpleSingleFingerTest();
    testMultipleSampleConsolidate();
    doSingleFingerMultipleImage();
    doQualityReferenceTest();
  }

  public void simpleSingleFingerTest() {
    // Obtain the image file for subject1, finger1, sample1
    int subjectId = 1;
    int[] fingers = this.FourFingers;

    SubjectEnrollmentReference enrollmentInfo =
        new SubjectEnrollmentReference(String.valueOf(subjectId));
    enrollmentInfo.setTargetSampleCount(1);
    enrollmentInfo.setTargetFingers(fingers);

    // Capture each finger successfully
    for (int finger : fingers) {
      // Initialize capture variables
      Boolean captured = false;
      int sampleIndex = 1;

      while (!captured) {

        String imageFile = TestDataManager.getImageFile(subjectId, finger, sampleIndex);
        // Utility function to load the image
        ImageData image = TestDataManager.loadImage(imageFile);

        // Create an image context
        ImageContext context = new ImageContext(image, finger);

        // Manage error feedback
        int status = context.getStatus();
        // Check for blocking error
        if (context.isBlocked()) {
          // If blocked this means there is no useful additional processing to be performed
          // Obtain error status information
          String errorDescription = context.getStatusDescription();
          // Display feedback
          PrintMessage(
              "Image is rejected with status code "
                  + status
                  + " and error description "
                  + errorDescription);
          // Increment sampleIndex to simulate moving forward to the next user presentation
          sampleIndex++;
          continue;
        } else if (ErrorStatusCodes.isWarning(status)) {
          // Indicates that this is a warning but template extraction is still possible
          String warningDescription = context.getStatusDescription();
          // Display warning feedback but continue processing
          PrintMessage(
              "Warning on image with status " + status + " description " + warningDescription);
        }

        // Create a finger enrollmewnt reference
        FingerEnrollmentReference fingerReference = new FingerEnrollmentReference(finger);
        fingerReference.addImageData(image, ImageData.FORMAT_WSQ);

        // Check the quality rank and score
        QualityInfo qInfo = context.getQualityInfo();
        QualityRank rank = context.getQualityInfo().getRank();
        int qScore = context.getQualityInfo().getQualityScore();

        ReferenceDataItem reference = null;

        // Check quality info by rank ..
        if (qInfo.getRank().compareTo(QualityRank.MEDIUM) < 0) {
          // Quality of the image is too low
          PrintMessage("Image rejected for further processing");
          reference = context.getReferenceData();
          fingerReference.addReferenceData(reference);

          // Add the reference to enrollment information
          enrollmentInfo.add(fingerReference);
          sampleIndex++;
          continue;
        }

        // Equivalent quality check based on standard quality threshold
        // Obtain the configured quality threshold
        double qualityThreshold =
            ImageContextSDK.getThreshold(GKThresholdParameter.MinutiaQualityThreshold);

        if (qInfo.getQualityScore() < qualityThreshold) {
          // Accumulate reference in case we need it
          reference = context.getReferenceData();
          fingerReference.addReferenceData(reference);

          // Add the reference to enrollment information
          enrollmentInfo.add(fingerReference);
          sampleIndex++;
          captured = false;
          continue;
        }

        // If we reach this point the image is okay and the quality is fine.
        reference = context.getReferenceData();
        fingerReference.addReferenceData(reference);
        // Add the reference to enrollment information
        enrollmentInfo.add(fingerReference);
        captured = true;
      }
    }
    // Purge samples
    enrollmentInfo.purgeSamples(true);

    // Print final reference state
    printHeaderResult("Enrollment state", enrollmentInfo.printReferenceState());

    try {
      // Export the Enrollment package in ABIS4 XML format
      String enrollmentXML = enrollmentInfo.toXml(GKXmlSchema.ABIS4);
      String xmlFile = getTestFile("enrolmentXml_" + subjectId, "xml", false);
      FileUtils.stringToFile(enrollmentXML, xmlFile);

      // Export the Enrollment package as binary encoding
      byte[] binaryEncoding = enrollmentInfo.getEncoding();
      String dataFile = getTestFile("enrolmentData_" + subjectId, "dat", false);
      FileUtils.byteArrayToFile(binaryEncoding, dataFile);
    } catch (IOException exception) {
      handleException(exception);
    }
  }

  /** */
  public void doSingleFingerMultipleImage() {
    doSingleFingerMultipleImage(false);
    doSingleFingerMultipleImage(true);
  }

  /**
   * Example scenario that emulates examination of multiple images from a single presentation.
   * Quality assessment is performed on each image, but reference is only extracted from the best
   * image.
   *
   * @param useFastQuality If set to true then use fast(er) rather than standard quality assessor
   */
  public void doSingleFingerMultipleImage(boolean useFastQuality) {
    ImageContextSDK.resetConfiguration();
    ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.FastQualityAssessor, useFastQuality);

    int finger = 3;
    int subject = 1;
    int[] samples = TestDataManager.generateArray(1, 5);
    List<ImageData> imageList = TestDataManager.loadImages(subject, finger, samples);

    List<ImageContext> contextList = new ArrayList<ImageContext>();
    ImageContext bestContext = null;
    QualityInfo bestQuality = null;
    int index = 0;
    for (ImageData image : imageList) {
      ImageContext context = new ImageContext(image, finger);
      StopWatch timer = new StopWatch();
      QualityInfo qInfo = context.getQualityInfo();
      if (bestQuality == null || qInfo.betterThan(bestQuality)) {
        bestQuality = qInfo;
        bestContext = context;
      }
      // Provide user feedback based on quality information
      doFeedback(qInfo, index);
      contextList.add(context);
      printTimerResult("Quality Assess", index, timer);
      ++index;
    }

    // We have if we want to use them 5 samples
    // We will just extract from best image only
    StopWatch timer = new StopWatch();
    ReferenceDataItem reference = bestContext.getReferenceData();
    printObject("Selected reference", reference);
    printTimerResult("Reference extract", timer);

    for (ImageContext cxt : contextList) {
      cxt.dispose();
    }
  }

  public void testMultipleSampleConsolidate() {
    int[] samples = Commons.generateRangeArray(1, 3);
    List<ReferenceDataItem> references = TestDataManager.loadReferences(1, 4, samples);
    boolean isConsistent = ReferenceDataItem.checkConsistentFingerID(references);
    printResult("Consistent", isConsistent);
    ReferenceDataItem consolidated = ReferenceDataItem.consolidateReferences(references);
    printObject("Consolidated", consolidated);
  }

  // For diagnostic purposes only
  public void doNullTest() {
    ImageContextSDK.resetConfiguration();
    ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, false);
    ImageData image = TestDataManager.loadImage(1, 3, 1);
    //		StopWatch timer = new StopWatch();
    ImageContext context = new ImageContext(image, 3);
    //		context.dispose();
  }

  public void doQualityReferenceTest() {
    ImageContextSDK.resetConfiguration();
    ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, false);
    ImageData image = TestDataManager.loadImage(1, 3, 1);
    doQualityReferenceTest(image, 3);
  }

  private void doQualityReferenceTest(ImageData image, int finger) {
    StopWatch timer = new StopWatch();
    ImageContext context = new ImageContext(image, finger);
    QualityInfo quality = context.getQualityInfo();
    printTimerResult("Quality assess", timer);
    printObject("Quality info", quality);

    // Now trigger reference extraction
    timer.sleep(2000);
    timer.reset();
    ReferenceDataItem reference = context.getReferenceData();
    printTimerResult("Reference extract", timer);
    printObject("Reference", reference);
    context.dispose();
  }

  public void imageRotateTest() {
    // Single finger example
    String imageFile = TestDataManager.getImageFile(1, 3, 5);
    ImageData image = TestDataManager.loadImage(imageFile);
    ImageRotateTest(image, "singleFinger");

    // Four finger example
    String fourFingerFile = getFourFingerImageFile(V2Left);
    ImageData slapImage = TestDataManager.loadImage(fourFingerFile);
    ImageRotateTest(slapImage, "slapImage");
  }

  public void ImageRotateTest(ImageData imageData, String name) {
    ImageData imageAligned = imageData.alignFingers();

    saveImage(imageData, name + "_input", true);

    saveImage(imageAligned, name + "_alignedFile", true);

    SegmentationRegion segment = imageData.getSegmentationRegion();
    printObject("segment", segment);
    if (segment != null) {
      ImageData image2 = imageData.extractSubImage(segment, true);

      saveImage(image2, name + "_image2");
      ImageData image3 = imageData.extractSubImage(segment, false);
      saveImage(image3, name + "_image3");
    }
  }
}
