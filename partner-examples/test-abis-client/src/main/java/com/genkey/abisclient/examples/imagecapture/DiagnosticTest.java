package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.FileUtils;
import java.util.List;

public class DiagnosticTest extends ExampleModule {

  @Override
  protected void runAllExamples() {
    //	fileExporeTest();
    libraryLoadTest();
    algorithmLoadTests();
  }

  public void algorithmLoadTests() {
    algorithmLoadTests(true);
    algorithmLoadTests(false);
  }

  private void algorithmLoadTests(boolean active) {
    String imageFile = TestDataManager.getImageFile(1, 1, 1);
    ImageData image = TestDataManager.loadImage(imageFile);
    List<String> algorithms = TestDataManager.getActiveFamilies(active);
    for (String algorithm : algorithms) {
      printMessage("Testing family " + algorithm);
      boolean isLoaded = ImageContextSDK.isAlgorithmLoaded(algorithm, image);
      if (!isLoaded) {
        int status = ImageContextSDK.checkAlgorithmLoaded(algorithm, image, true);
        printResult(algorithm + "load status", status);
      } else {
        printResult(algorithm + " loaded", isLoaded);
      }
    }

    printMessage("Algorithm Load tests complete");
  }

  public void fileExporeTest() {
    String configPath = FileUtils.getConfigurationPath();
    int level = 2;
    String fileDisplay = ImageContextSDK.exploreFileSystem(configPath, level);
    printHeader(configPath);
    printMessage(fileDisplay);
  }

  static final String[] LibraryNames = {"GKBioFinger_INT", "GKFastAFIS_INT"};

  static final String[] FunctionNames = {"GKBioFinger_Open", "GKFastAFIS_Open"};

  public void libraryLoadTest() {
    for (int ix = 0; ix < LibraryNames.length; ix++) {
      libraryLoadTest(LibraryNames[ix], FunctionNames[ix]);
    }
  }

  private void libraryLoadTest(String libPath, String loadFunction) {
    int result = ImageContextSDK.exploreLibraryLoad(libPath, loadFunction);
    printResult(libPath, result);
  }
  ;
}
