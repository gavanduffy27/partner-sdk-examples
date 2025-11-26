package com.genkey.abisclient.examples.misc;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;

public class AdHocTest extends ExampleModule {

  @Override
  protected void runAllExamples() {
    testReplayImage();
    initializationTests();
    testSDKProperties();
    showLibraryDependencies();
  }

  @Override
  protected void setUp() {
    super.setUp();
  }

  public void testReplayImage() {
    String imageFile = "./testFiles/replayFile2.b64";
    String format = ImageData.FORMAT_WSQ;
    try {
      byte[] data = FileUtils.byteArrayFromBase64File(imageFile);
      ImageData imageData = new ImageData(data, format);
      String wsqFile = FileUtils.forceExtension(imageFile, format);
      byte[] encoding = imageData.asEncodedImage(format);
      FileUtils.byteArrayToFile(encoding, wsqFile);
      format = ImageData.FORMAT_BMP;
      String bmpFile = FileUtils.forceExtension(imageFile, format);
      encoding = imageData.asEncodedImage(ImageData.FORMAT_BMP);
      FileUtils.byteArrayToFile(encoding, bmpFile);

      ImageContext context = new ImageContext(imageData, 1);
      int status = context.getStatus();
      printResult("Status", status);
      if (status != 0) {
        boolean isBlocked = context.isBlocked();
        printResult("Blocked", isBlocked);
      }
      ReferenceDataItem refData = context.getReferenceData();
      double score = refData.getQualityScore();
      printResult("Score", score);

    } catch (Exception e) {
      super.handleException(e);
    }
  }

  public void testSDKProperties() {
    String appName = ABISClientLibrary.getAndroidApplicationName();
    super.printResult(ABISClientLibrary.PARAM_ANDROID_APPNAME, appName);
    ABISClientLibrary.setAndroidApplicationName("com.genkey.product-1");
    appName = ABISClientLibrary.getAndroidApplicationName();
    super.printResult(ABISClientLibrary.PARAM_ANDROID_APPNAME, appName);
  }

  public void showLibraryDependencies() {
    for (String libraryName : ABISClientLibrary.androidLibraries) {
      super.printMessage(libraryName);
    }
  }

  public static void main(String[] args) {
    AdHocTest example = new AdHocTest();
    example.runTestExamples();
  }

  public void initializationTests() {
    // Main default initialization has already occurred. Make no assumptions

    showEnrolmentConfig();

    // Make sure we know the initial version
    int currentVersion = ABISClientLibrary.getActiveVersion();

    // Ensure version 4 is loaded, but do not make this the default
    ABISClientLibrary.activateVersion(4, false);
    showEnrolmentConfig();

    ABISClientLibrary.activateVersion(8, true);
    showEnrolmentConfig();

    ABISClientLibrary.setEnrollmentVersion(4);
    showEnrolmentConfig();

    ABISClientLibrary.setEnrollmentVersion(8);
    showEnrolmentConfig();

    // Restore the previously active version
    ABISClientLibrary.setEnrollmentVersion(currentVersion);
  }

  private void showEnrolmentConfig() {
    //		super.setHighConcurrency();
    String imageFile = TestDataManager.getImageFile(1, 1, 1);
    ImageData image = TestDataManager.loadImage(imageFile);
    ImageContext context = new ImageContext(image, 1);
    ReferenceDataItem referenceData = context.getReferenceData();
    int sizeFull = referenceData.size();
    referenceData.compactSelf();
    this.printObject("ReferenceData (Compact)", referenceData.printReferenceState());
    int sizeCompact = referenceData.size();
    referenceData.truncate();
    int sizeTruncate = referenceData.size();
    this.printObject("ReferenceData (Truncate)", referenceData.printReferenceState());

    printResult("Size Full", sizeFull);
    printResult("Size Compact", sizeCompact);
    printResult("Size Truncated", sizeTruncate);
  }
}
