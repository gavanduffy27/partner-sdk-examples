package com.genkey.partner.workshop;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.partner.example.PartnerExample;

public class TestAppTests extends BMSWorkshopExample {

  private static final String SegmentationFileName = "dgie_test_l2";

  public static void main(String[] args) {
    PartnerExample test = new TestAppTests();
    test.processCommandLine(args);
  }

  protected void runAllExamples() {
    segmentationTest();
  }

  public void segmentationTest() {
	  TestDataManager.setFingerImagePath(4);
	  String imageFile = TestDataManager.getImageFile(SegmentationFileName);
	  if (! FileUtils.existsFile(imageFile)) {
		  return;
	  }
	  String fName = FileUtils.baseName(imageFile);
	  ImageData imageData = TestDataManager.loadImage(imageFile);
	  //ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
	  ImageContext context = new ImageContext(imageData, RightHand);
	  int count = context.count();
	  int [] detectedFingers=  context.getDetectedFingers();
	  String exportFolder = FileUtils.expandConfigPath("test/exports/dgie");
	  String exportFormat = ImageData.FORMAT_BMP;
	  for(int ix=0; ix < context.count(); ix++) {
		  ImageData segment = context.extractImageSegment(ix);
		  int fingerId = detectedFingers[ix];
		  String baseName = String.format("%s_%d_%d", fName, ix, fingerId);
		  byte [] encoding = segment.asEncodedImage(exportFormat);
		  String fileName = FileUtils.expandConfigFile(exportFolder, baseName, exportFormat);
		  try { 
			  FileUtils.byteArrayToFile(encoding, fileName);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  
	  }
  }
}
