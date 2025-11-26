package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.EnrolmentSettings;
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
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SegmentationExample extends ExampleModule {

  static String ThumbsFile = "Thumbs_01";
  static String TwoFingerFile = "twoFinger";
  static String MixedFingerFile = "mixedFinger";
  static String V1Left = "v1-left";
  static String V2Left = "v2-left";

  boolean displayImages = true;

  public static void main(String[] args) {
    ExampleModule test = new SegmentationExample();
    test.runTestExamples();
  }

  @Override
  protected void setUp() {
    super.setUp();
    TestDataManager.setImageDirectory(ExampleModule.TwoFingerDirectory);
    TestDataManager.setImageFormat("bmp");
  }

  @Override
  protected void runAllExamples() {
    fourFingerExamples();
    twoFingerExamples();
  }

  public void simpleMultipleFingerExample() {

    double alignmentMax = ImageContextSDK.getThreshold(GKThresholdParameter.AlignmentMax);
    printResult("Alignment Max", alignmentMax);

    String subjectName = "exampleSubject";
    int[] fingers1 = LeftHandFingers;
    int[] fingers2 = Commons.generateRangeArray(7, 4);
    int[] fingers = Commons.generateRangeV(7, 8, 9, 10);

    SubjectEnrollmentReference enrollmentData = new SubjectEnrollmentReference(subjectName);
    enrollmentData.setTargetFingers(fingers);
    enrollmentData.setQualityThreshold(15);
    enrollmentData.setTargetSampleCount(2);

    // First capture
    ImageData image1 = getFourFingerImage(V1Left);
    ImageData image2 = getFourFingerImage(V2Left);
    List<ImageData> images = new ArrayList<ImageData>();
    images.add(image1);
    images.add(image2);

    int sampleIndex = 0;
    boolean isComplete = enrollmentData.isComplete();

    // Exit when no more images or enrollment is complete
    while (sampleIndex < images.size() && !enrollmentData.isComplete()) {
      // Show which fingers still require more samples
      int[] requiredFingers = enrollmentData.getFingersIncomplete();
      ImageData image = images.get(sampleIndex);
      ImageContext imageContext = new ImageContext(image, fingers);
      long contextStatus = imageContext.getStatus();
      if (!ErrorStatusCodes.isSuccess(contextStatus)) {
        printResult("Status", imageContext.getStatusDescription());
      }
      int imageCount = imageContext.count();

      int[] fingersDetected = imageContext.getDetectedFingers();

      if (!ErrorStatusCodes.isSuccess(contextStatus)) {
        printResult("Status", imageContext.getStatusDescription());
        if (contextStatus == ErrorStatusCodes.INCONSISTENT_FINGER_IDS) {
          printResult("Expected fingers", fingers);
          printResult("Detected fingers", fingersDetected);
          sampleIndex++;
          // Operator decides
          continue;
        }

        if (contextStatus == ErrorStatusCodes.INVALID_HAND_PRESENTATION) {
          SegmentationRegion region = imageContext.getSegmentationRegion(0);
          double alignment = region.getAlignment();
          double permissiveThreshold = alignment + 0.02;

          // Long winded manner update
          EnrolmentSettings settings = EnrolmentSettings.getInstance();
          settings.syncLoad();
          EnrolmentSettings.Threshold threshold = settings.getThresholdSettings();
          double maxAlignment = threshold.AlignmentMax;
          threshold.AlignmentMax = permissiveThreshold;
          threshold.applyThresholds();

          // check the new value
          double currentMax = ImageContextSDK.getThreshold(GKThresholdParameter.AlignmentMax);
          printResult("New maximum", currentMax);

          // go again
          imageContext = new ImageContext(image, fingers);
          contextStatus = imageContext.getStatus();

          printResult("New status", contextStatus);
          if (!ErrorStatusCodes.isSuccess(contextStatus)) {
            // oh no!
            printResult("Status", imageContext.getStatusDescription());
          }
        }

        if (imageContext.isBlocked()) {
          printMessage(
              "No further processing on blocked image with status "
                  + imageContext.getStatusDescription());
          sampleIndex++;
          continue;
        }
      }

      // Access the image segments from left to right
      for (int ix = 0; ix < imageCount; ix++) {
        int finger = fingersDetected[ix];
        SegmentationRegion segment = imageContext.getSegmentationRegion(ix);
        printResult("Segment alignment", segment.getAlignment());
        printHeaderResult("Segment for finger " + finger, segment.toString());
      }

      // Diagnostic routing to export images to a BMP file
      String segmentFile = getTestFile("imageSegments", "bmp", true);
      // ImageContextSDK
      imageContext.displaySegments(segmentFile, true, true);

      // Access the actual segments and a
      List<FingerEnrollmentReference> fingerReferences = new ArrayList<FingerEnrollmentReference>();
      for (int ix = 0; ix < imageCount; ix++) {
        int finger = imageContext.getFingers()[ix];
        QualityInfo qInfo = imageContext.getQualityInfo(ix);
        if (qInfo.getRank().compareTo(QualityRank.MEDIUM) < 0) {
          PrintMessage(
              "Warning quality for "
                  + finger
                  + " on sample "
                  + sampleIndex
                  + " is less than target");
        }
        this.printIndexResult("Quality Score", ix, qInfo.getQualityScore());
        ImageData segmentImage = imageContext.extractImageSegment(ix, true);
        try {
          ReferenceDataItem referenceData = imageContext.getReferenceData(ix);
          FingerEnrollmentReference fingerEnrollmentReference =
              new FingerEnrollmentReference(finger);
          fingerEnrollmentReference.addImageData(segmentImage, ImageData.FORMAT_WSQ);
          fingerEnrollmentReference.addReferenceData(referenceData);
          enrollmentData.add(fingerEnrollmentReference);
        } catch (Exception e) {
          long status = imageContext.getStatus();
          String errorString = imageContext.getStatusDescription();
          e.printStackTrace();
          handleException(e, false);
          sampleIndex++;
          continue;
        }
      }
      // Show the current enrollment status
      printHeaderResult("Enrollment reference status", enrollmentData.printReferenceState());

      // Check for completion of enrollment
      if (sampleIndex >= images.size()) {
        PrintMessage("Exiting capture because no more images");
        break;
      } else if (enrollmentData.isComplete()) {
        PrintMessage("Exiting capture because enrollment is complete");
        break;
      } else {
        // continue with enrollment on next sample
        sampleIndex++;
        continue;
      }
    }
    // No more samples or else enrollment is complete
    enrollmentData.sortReferences();
    printHeaderResult("Final Enrollment status", enrollmentData.printReferenceState());

    // Throw away unwanted low quality samples
    enrollmentData.purgeSamples(false);

    // Generate and export XML document for enrollment data
    String xmlContent = enrollmentData.toXml(GKXmlSchema.ABIS4);
    String xmlFile = getTestFile("enrollmentData_" + subjectName, "xml");
    try {
      FileUtils.stringToFile(xmlContent, xmlFile);
    } catch (Exception e) {
      handleException(e);
    }
  }

  public boolean isDisplayImages() {
    return displayImages && this.isUseGui();
  }

  public void setDisplayImages(boolean displayImages) {
    this.displayImages = displayImages;
  }

  public void twoFingerExamples() {
    TestDataManager.setImageDirectory(TwoFingerDirectory);

    printHeader("Testing Full Extract synchronous/mt");
    setFullExtract(false, true);
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});

    printHeader("Testing Full Extract asynchronous/mt");
    setFullExtract(true, true);
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});

    printHeader("Testing Quality only synchronous/mt");
    setQualityOnly(false, true);
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});

    printHeader("Testing Quality only asynchronous/mt");
    setQualityOnly(true, true);
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});

    printHeader("Testing Fast Quality asynchronous/mt");
    setFastQuality(true, true);
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});

    printHeader("Testing segmentation only");
    setSegmentationOnly();
    doSingleImageExample(ThumbsFile, new int[] {6, 1});
    doSingleImageExample(TwoFingerFile, new int[] {8, 7});
  }

  public void fourFingerExamples() {
    TestDataManager.setImageDirectory(FourFingerDirectory);
    int[] fingers = Commons.generateRangeV(8, 7, 2, 3);
    doSingleImageExample(MixedFingerFile, fingers);
  }

  private static void setFullExtract(boolean asynchronous, boolean multiThreaded) {
    // Reset system defaults
    ImageContextSDK.resetConfiguration();

    // Run both quality assessment and reference extraction by default
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);

    // Set threading as specified
    ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsQualityAssess, multiThreaded);
    ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsReferenceExtract, multiThreaded);

    // Set asynchronous as specified. Setting true detaches the processing from segmentation.
    // If set to false (i.e. synchronous) the processing occurs within constructor along with
    // segmentation.
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, asynchronous);
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadReferenceExtract, asynchronous);

    // These are the defaults
    ImageContextSDK.setSwitch(GKSwitchParameter.FastQualityAssessor, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);
  }

  private static void setSegmentationOnly() {
    // Switching these off means only segmentation gets triggered
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, false);
  }

  private static void setOptimalFlow() {
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);

    // Run all image processing apart from segmentation as background thread
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadReferenceExtract, true);

    // Set QA in real time as part of screen interaction so set multithreaded
    ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsQualityAssess, true);

    // Let reference extraction run as a single background thread
    ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsReferenceExtract, false);
  }

  private static void setQualityOnly(boolean asynchronous, boolean multiThreaded) {
    setFullExtract(asynchronous, multiThreaded);
    // Override this default
    ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);

    // Run only quality assessment and reference extraction by default
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, false);
    ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);
    ImageContextSDK.setSwitch(GKSwitchParameter.FastQualityAssessor, false);
  }

  private static void setFastQuality(boolean asynchronous, boolean multiThreaded) {
    setQualityOnly(asynchronous, multiThreaded);
    // use fast quality assessment
    ImageContextSDK.setSwitch(GKSwitchParameter.FastQualityAssessor, true);
  }

  public void twoFingerExample(String imageFile, int[] fingers) {
    TestDataManager.setFingerImagePath(fingers.length);
    String fileName = TestDataManager.resolveFileName(imageFile);

    if (StringUtils.isNullString(fileName)) {
      this.printMessage("Filename " + imageFile + " not found on all paths");
      return;
    }

    if (FileUtils.isAbsolutePath(fileName)) {
      printResult("Source image file", fileName);
    }
    String baseName = FileUtils.baseName(fileName);

    printHeader("Segmentation timer tests on " + baseName, '*');
    printHeader("Testing Full Ext1ract synchronous/mt");
    setFullExtract(false, true);
    doSingleImageExample(fileName, fingers);

    PrintHeader("Testing Full Extract asynchronous/mt");
    setFullExtract(true, true);
    doSingleImageExample(fileName, fingers);

    printHeader("Testing Quality only synchronous/mt");
    setQualityOnly(false, true);
    doSingleImageExample(fileName, fingers);

    PrintHeader("Testing Quality only asynchronous/mt");
    setQualityOnly(true, true);
    doSingleImageExample(fileName, fingers);

    printHeader("Testing Fast Quality asynchronous/mt");
    setFastQuality(true, true);
    doSingleImageExample(fileName, fingers);
  }

  /**
   * Processes a multiple finger issues ..
   *
   * @param imageFile
   * @param fingers
   */
  public void doSingleImageExample(String imageFile, int[] fingers) {
    TestDataManager.BookMark bookmark = new TestDataManager.BookMark();
    TestDataManager.setFingerImagePath(fingers.length);
    printHeader("Analyzing new image " + imageFile, '-');
    ImageData image = TestDataManager.loadImage(imageFile);
    StopWatch timer = new StopWatch();
    ImageContext context = new ImageContext(image, fingers);

    int[] detected = context.getDetectedFingers();
    printResult("Detected fingers", Commons.arrayToList(detected));
    int[] assigned = context.getFingers();

    // check if fingers detected were consistent
    if (!context.isFingersConsistent()) {
      int[] fingersDetected = context.getDetectedFingers();
      printResult("Fingers assigned", Commons.arrayToList(fingers));
      printResult("Fingers detected", Commons.arrayToList(fingersDetected));
      String message = "Fingers detected not the same as assigned";
      /*
            	if ( ! promptOperatorContinue(message)) {
            		return;
            	}
      */
    }

    // Example routine to show what interrogation facilities exist
    if (!checkErrorHandler(context)) {
      return;
    }

    printTimerResult("Context create", timer);

    List<ImageData> imageSegments = CollectionUtils.newList();
    // Obtain segments
    for (int ix = 0; ix < context.count(); ix++) {
      SegmentationRegion segment = context.getSegmentationRegion(ix);
      ImageData imageSegment = context.extractImageSegment(ix, true);
      imageSegments.add(imageSegment);
      printObject("Segment[" + ix + "]", segment);
    }
    printTimerResult("Segmentation", timer);

    // Now check quality assessment
    int totalScore = 0;
    for (int ix = 0; ix < context.count(); ix++) {
      QualityInfo qInfo = context.getQualityInfo(ix);
      totalScore += qInfo.getQualityScore();
      printTimerResult("Quality segment accessed", ix, timer);
    }
    // Save image context and capture next fingers

    // Later ...
    printHeader("Reference Extraction");
    StopWatch refTimer = new StopWatch();
    for (int ix = 0; ix < context.count(); ix++) {
      ReferenceDataItem reference = context.getReferenceData(ix);
      printTimerResult("Reference", ix, refTimer);
    }
    printTimerResult("Reference extraction complete", refTimer);

    processForBiometricProfile(context);

    context.dispose();
    bookmark.Restore();
  }

  public static ImageData getFourFingerImage(String baseName) {
    String fileName = TestDataManager.getImageFile(baseName, TestDataManager.FourFingerPath);
    return TestDataManager.loadImage(fileName);
  }

  public static ImageData getTwoFingerImage(String baseName) {
    TestDataManager.BookMark bookMark = new TestDataManager.BookMark();
    TestDataManager.setImageDirectory(TestDataManager.TwoFingerPath);
    String fileName = TestDataManager.getImageFile(baseName);
    bookMark.Restore();
    return TestDataManager.loadImage(fileName);
  }

  static class ProfileDataItem {
    ReferenceDataItem fingerTemplate;
    ImageData fingerImage;
    int fingerIndex;

    public ProfileDataItem(int fingerIndex, ReferenceDataItem reference, ImageData image) {
      this.setFingerIndex(fingerIndex);
      this.setFingerImage(image);
      this.setFingerTemplate(reference);
    }

    public ReferenceDataItem getFingerTemplate() {
      return fingerTemplate;
    }

    public void setFingerTemplate(ReferenceDataItem fingerTemplate) {
      this.fingerTemplate = fingerTemplate;
    }

    public ImageData getFingerImage() {
      return fingerImage;
    }

    public void setFingerImage(ImageData fingerImage) {
      this.fingerImage = fingerImage;
    }

    public int getFingerIndex() {
      return fingerIndex;
    }

    public void setFingerIndex(int fingerIndex) {
      this.fingerIndex = fingerIndex;
    }

    /**
     * Return the binary encoding of the reference data
     *
     * @return
     */
    public byte[] getReferenceEncoding() {
      return fingerTemplate.getReferenceData();
    }

    /**
     * Return the binary encoding for the image data
     *
     * @return
     */
    public byte[] getImageEncoding() {
      return fingerImage.asEncodedImage(ImageData.FORMAT_WSQ);
    }
  }

  /**
   * Mock class container for biometric data that goes into the BiometricReferenceProfile
   *
   * @author gavan
   */
  static class BiometricReferenceProfile extends HashMap<Integer, ProfileDataItem> {

    private static final long serialVersionUID = 1L;

    /**
     * Add profile information for specified finger
     *
     * @param fingerIndex
     * @param reference
     * @param image
     */
    public void addFinger(int fingerIndex, ReferenceDataItem reference, ImageData image) {
      this.put(fingerIndex, new ProfileDataItem(fingerIndex, reference, image));
    }

    /**
     * Return the fingers present in the container
     *
     * @return
     */
    public Set<Integer> getFingers() {
      return this.keySet();
    }

    /**
     * Return the profileData for specified finger
     *
     * @param fingerIndex
     * @return
     */
    public ProfileDataItem getProfileDataForFinger(int fingerIndex) {
      return get(fingerIndex);
    }

    /**
     * Return reference encoding for specified finger
     *
     * @param finger
     * @return
     */
    public byte[] getReferenceEncoding(int finger) {
      return getProfileDataForFinger(finger).getReferenceEncoding();
    }

    /**
     * Return image encoding for specified finger
     *
     * @param finger
     * @return
     */
    public byte[] getImageEncoding(int finger) {
      return getProfileDataForFinger(finger).getImageEncoding();
    }
  }

  private void processForBiometricProfile(ImageContext context) {
    List<ImageData> segments = new ArrayList<>();
    List<ReferenceDataItem> templates = new ArrayList<>();

    List<ImageData> imageDisplayList = new ArrayList<>();

    int[] fingers = context.getFingers();

    BiometricReferenceProfile profile = new BiometricReferenceProfile();

    for (int ix = 0; ix < context.count(); ix++) {
      ReferenceDataItem referenceItem = context.getReferenceData(ix);
      ImageData imageData = context.extractImageSegment(ix, true);
      int fingerIndex = fingers[ix];
      profile.addFinger(fingerIndex, referenceItem, imageData);
      imageDisplayList.add(imageData);
    }

    if (this.isDisplayImages()) {
      super.showImageList(imageDisplayList, "ImageSegments", 0.25);
      // this.getUserMessageHandler().showImageList("Image Segments"), imageDisplayList, 0, 0,
      // 0.251);
    }
  }

  private boolean checkErrorHandler(ImageContext context) {
    int status = context.getStatus();

    // Direct from context
    String errorDescription = context.getErrorDescription();

    // General look up for any error
    String errorDescription2 = ErrorStatusCodes.getDescription(status);

    // is this successful
    boolean isSuccess = ErrorStatusCodes.isSuccess(status);

    if (isSuccess) {
      return true;
    }

    // is this just a warning code
    boolean isWarning = ErrorStatusCodes.isWarning(status);

    if (isWarning) {
      // Display message to operator and permit override
      if (!promptOperatorContinue(errorDescription)) {
        return false;
      }
    }

    // Does this condition preclude further processing
    boolean isBlock = ErrorStatusCodes.isBlockingError(status);

    return !isBlock;
  }

  boolean checkImageStatus(ImageContext context) {
    long status = context.getStatus();
    if (status < 0) {
      // String message = ImageContextSDK.getErrorDescription(status);
    }
    return false;
  }
}
