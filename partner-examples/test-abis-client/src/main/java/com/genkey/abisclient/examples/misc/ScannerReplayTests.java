package com.genkey.abisclient.examples.misc;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.Minutia;
import com.genkey.abisclient.MinutiaTemplate;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerReplayTests extends ExampleModule {

  static final String ProblemImageDirectory = FileUtils.expandConfigPath("replay/images/mar19");
  static final String UserImageDirectory = FileUtils.expandConfigPath("replay/images/mar19b");
  static final String SergeImageDirectory = FileUtils.expandConfigPath("images/problems/dgie");

  public static void main(String[] args) {
    ScannerReplayTests test = new ScannerReplayTests();
    // Initialize the SDK
    test.setUp();
    test.runAllExamples();
  }

  // This will initialize previous generation matcher
  static void initializeLegacyMatcher() {
    ABISClientLibrary.activateVersion(4, true);
  }

  static void initializeR8Matcher() {
    ABISClientLibrary.activateVersion(8, true);
    ABISClientLibrary.setEnrollmentVersion(8);
  }

  @Override
  protected void setUp() {
    // SDK Calls ..
    ABISClientLibrary.initializeDefault();

    // Note what version is initialized by default
    int activeVersion = ABISClientLibrary.getActiveVersion();

    // Note it uses version 8 by default but we can also make this explicit - version 8 is BioFinger
    initializeR8Matcher();
    super.setUp();
  }

  @Override
  protected void runAllExamples() {
    //	showMinutiaTemplateAccess();
    problemImageTest();
    //	userImageTests();
  }

  public void problemImageTest() {
    analyzeImageSet(SergeImageDirectory, ImageData.FORMAT_BMP);
  }

  public void showImageConversions() {
    ImageData image = getSingleTestImage();
    showImageConversionRoutines(image);
  }

  private void showImageConversionRoutines(ImageData image) {
    // EXPORTIng from an instance of ImageData
    // Any instantiate instances of ImageData can be exported in any of the supported formats
    byte[] bmpEncoding = image.asEncodedImage(ImageData.FORMAT_BMP);
    byte[] wsqEncoding = image.asEncodedImage(ImageData.FORMAT_WSQ);
    byte[] jpeg2KEncoding = image.asEncodedImage(ImageData.FORMAT_JP2);
    byte[] pgmEncoding = image.asEncodedImage(ImageData.FORMAT_PGM);

    // We can regenerate an instance from any format by using the same constructor format
    // but specifying image format as second parameter/

    // Importing to ImageData from an external format - just use constructor
    ImageData wsqImage = new ImageData(wsqEncoding, ImageData.FORMAT_WSQ);
    ImageData jp2Image = new ImageData(jpeg2KEncoding, ImageData.FORMAT_JP2);
    ImageData bmpImage = new ImageData(bmpEncoding, ImageData.FORMAT_BMP);
    ImageData pgmImage = new ImageData(pgmEncoding, ImageData.FORMAT_PGM);

    // Or from any instance of BufferedImage - import/export

  }

  /** Show how to access minutia template */
  public void showMinutiaTemplateAccess() {
    ReferenceDataItem referenceData = getSingleTestReference();
    showStructureRoutines(referenceData);
  }

  private void showStructureRoutines(ReferenceDataItem referenceData) {
    // For access we need to create a Structured Template
    StructuredTemplate sTemplate = new StructuredTemplate(referenceData);
    MinutiaTemplate minutiaTemplate = sTemplate.getMinutiaTemplate();

    navigateMinutiaTemplate(minutiaTemplate);
    printObject("Minutia template", minutiaTemplate);

    byte[] isoTemplate = sTemplate.getISOEncoding();
    printResult("ISO template", isoTemplate.length);

    byte[] isoTemplate2 = minutiaTemplate.encodeISO();

    MinutiaTemplate minutiaTemplate2 = new MinutiaTemplate();
    MinutiaTemplate minutiaTemplate3 = new MinutiaTemplate();

    minutiaTemplate2.decodeISO(isoTemplate);
    minutiaTemplate3.decodeISO(isoTemplate2);

    printObject("MinutiaTemplate2", minutiaTemplate2);
    printObject("MinutiaTemplate3", minutiaTemplate3);
  }

  private void navigateMinutiaTemplate(MinutiaTemplate minutiaTemplate) {
    int count = minutiaTemplate.getMinutiaCount();
    for (Minutia minutia : minutiaTemplate.getMinutiaVector()) {
      int angle = minutia.getAngle();
      int x = minutia.getX();
      int y = minutia.getY();
      int type = minutia.getMinutiaType();
      int quality = minutia.getQuality();
      String minutiaDescription = minutia.toString();
    }
    String templateDescription = minutiaTemplate.toString();
  }

  public static ImageData getSingleTestImage() {
    String imageFileName =
        FileUtils.getFilenames(ProblemImageDirectory, ImageData.FORMAT_BMP, true)[0];
    ImageData image = TestDataManager.loadImage(imageFileName);
    return image;
  }

  static ReferenceDataItem getSingleTestReference() {
    ImageData image = getSingleTestImage();
    ImageContext context = new ImageContext(image, 1);
    return context.getReferenceData();
  }

  public void analyzeImageSet(String dirName, String format) {
    String[] fileNames = FileUtils.getFilenames(dirName, format, true);
    Map<String, ReferenceDataItem> referenceMap = new HashMap<>();

    // Extract the templates from the filenames
    for (String fileName : fileNames) {
      try {

        // obtain the file short name
        String baseName = FileUtils.baseName(fileName);

        printMessage("Processing file " + fileName);

        // First create an IMAGE object
        byte[] imageData = FileUtils.byteArrayFromFile(fileName);

        // SDK Call to instantiate an Image from raw encoding
        ImageData image = new ImageData(imageData, ImageData.FORMAT_BMP);

        // SDK Call to create an ImageContext and specify finger as 2 (NIST convention assumed)
        // int finger=2;
        int[] fingers = {10, 9, 8, 7};
        ImageContext context = new ImageContext(image, fingers);

        int status = context.getStatus();

        if (status != 0) {
          printMessage(context.getErrorDescription());
        }

        if (ImageContextSDK.isBlockingError(status)) {
          continue;
        }

        int count = context.count();

        try {
          // Access the generic template
          ReferenceDataItem referenceData = context.getReferenceData();

          // Add this template to the map structure
          referenceMap.put(baseName, referenceData);
        } catch (Exception e) {
          PrintMessage("Failure in processing of " + fileName);
          ExampleModule.handleException(e);
        }

      } catch (Exception e) {
        ExampleModule.handleException(e);
      }
    }

    List<String> keys = new ArrayList<>(referenceMap.keySet());

    // set the match threshold as 30 (equivalent to FAR of 1E-3)
    double threshold = 30;

    // Lets match every pair
    for (int ix = 0; ix < keys.size() - 1; ix++) {
      String file1 = keys.get(ix);
      ReferenceDataItem reference1 = referenceMap.get(file1);

      // Match this against the remaining items in the list
      for (int iy = ix + 1; iy < keys.size(); iy++) {
        String file2 = keys.get(iy);
        ReferenceDataItem reference2 = referenceMap.get(file2);

        // Compute the match score
        double score = ImageContextSDK.matchReferences(reference1, reference2);
        printMatchResult(file1, file2, score, threshold);
      }
    }
  }

  private void printMatchResult(String file1, String file2, double score, double threshold) {
    if (score >= threshold) {
      this.printMessage("Successful match for " + file1 + "/" + file2 + "::score=" + score);
    } else {
      this.printMessage(
          "Match failure for "
              + file1
              + "/"
              + file2
              + "::score="
              + score
              + " with threshold of "
              + threshold);
    }
  }

  public void userImageTests() {
    String[] dirNames = FileUtils.getSubDirectories(UserImageDirectory, true);
    for (String dirName : dirNames) {
      String baseName = FileUtils.baseName(dirName);
      FormatUtils.printBanner("Testing images for " + baseName);
      analyzeImageSet(dirName, ImageData.FORMAT_BMP);
    }
  }
}
