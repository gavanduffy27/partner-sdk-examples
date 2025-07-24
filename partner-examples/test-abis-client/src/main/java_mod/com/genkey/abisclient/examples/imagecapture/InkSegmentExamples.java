package com.genkey.abisclient.examples.imagecapture;

import java.awt.image.BufferedImage;
import java.util.List;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.ABISClientUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.StringUtils;

public class InkSegmentExamples  extends ExampleModule{
	
	protected static final String TestSegmentDirectory = "replay/hajo/t2";
	
	public static void main(String [] args) {
		InkSegmentExamples test = new InkSegmentExamples();
		test.runTestExamples();
	}
	

	@Override
	protected void runAllExamples() {
		//this.scanInkExamples();
		//this.reverseSampleTests();
	}
	

	static int [] inferFingersFromName(String fileName) {
		String baseName = FileUtils.baseName(fileName).toLowerCase();
		int [] result = null;
		if (baseName.indexOf("hand") >= 0 ) {
			if (baseName.indexOf("right") >= 0) {
				result = RightHandFingers;
			} else {
				result = LeftHandFingers;
			}
		} else if ( baseName.indexOf("thumb") >= 0) {
			result = Thumbs;
		} else {
			result = UnknownFingerList;
		}
		return result;
	}
	
	static int modShiftFingerID(int finger, int shift) {
		finger += shift;
		if (finger > 10) {
			finger -= 10;
		}
		return finger;
	}
	
	/*
	public void scanInkExamples() {
		TestDataManager.setImageDirectory(TestSegmentDirectory);
		TestDataManager.setImageFormat("pgm");
		List<String> imageNames = TestDataManager.getImageFileNames();
		int index=0;
		for(String imageName : imageNames) {
			ImageData imageData = loadResampledImage1(imageName, 600, 500);
			
			byte [] wsqEncoding = imageData.asEncodedImage(ImageData.FORMAT_WSQ);
			printResult("WsqEncoding", wsqEncoding.length);
			
			int [] fingers = inferFingersFromName(imageName);
			ImageContext context = new ImageContext(imageData, fingers);
			//ImageContextSDK.
			int [] detectedFingers = context.getDetectedFingers();
			for(int ix=0; ix < detectedFingers.length; ix++) {
				int expected= detectedFingers[ix];
				// Reference Data item constructed - any old way
				ReferenceDataItem refItem = context.getReferenceData(ix);
				printResult("Reference item", refItem);
				printResult("Modified Reference item", refItem.printReferenceState());
				int modFinger = modShiftFingerID(expected, 5);
				
				// Change the finger of an existing reference
				refItem.reassignFingerIndex(modFinger);
				printResult("Modified Reference item", refItem);
				printResult("Modified Reference item", refItem.printReferenceState());
				
				
				// Assign the finger index at the point of construction which will override
				// the internal finger assignment that is present in the encoding
				byte [] referenceData = refItem.getReferenceData();
				int modFinger2 = modShiftFingerID(modFinger, 3);
				ReferenceDataItem refItem2 = new ReferenceDataItem(referenceData, modFinger2);
				printResult("Modified Reference item 2", refItem2);
			}			
		}
		
	}
	*/
	
	/*
	public void segmentInkExamples() {
		segmentInkExamples(1000, false);
		segmentInkExamples(600, false);
		segmentInkExamples(500, false);
	}
	
	public void segmentInkExamples(int sourceResolution, boolean showSegments) {
		String exportDirectory = "debug/segment/scan600";
		TestDataManager.setImageDirectory(TestSegmentDirectory);
		TestDataManager.setImageFormat("pgm");
		List<String> imageNames = TestDataManager.getImageFileNames();
		for(String imageName : imageNames) {
			ImageData imageData = loadResampledImage1(imageName, sourceResolution, 500);			
			String baseName1 = StringUtils.concat(imageName, "slap",sourceResolution);
			//String exportFile = FileUtils.expandConfigFile(exportDirectory, baseName, ImageData.FORMAT_WSQ);
			exportImage(imageData, baseName1, exportDirectory);
			
			
			if (showSegments) {
				int [] fingers = inferFingersFromName(imageName);
				ImageContext context = new ImageContext(imageData, fingers);
				//ImageContextSDK.
				int [] detectedFingers = context.getDetectedFingers();
				for(int ix=0; ix < detectedFingers.length; ix++) {
					ImageData segmentImage = context.extractImageSegment(ix, false);
					String baseName = StringUtils.concat(imageName, ix, sourceResolution);
					exportImage(segmentImage, baseName, exportDirectory);
				}
			}
		}		
	}
	*/
	
	
	public void reverseSampleTests() {
		reverseSampleTests(500);
		reverseSampleTests(600);
	}
	
	public void reverseSampleTests(int sourceResolution) {
		TestDataManager.setImageDirectory(TestSegmentDirectory);
		TestDataManager.setImageFormat("pgm");
		List<String> imageNames = TestDataManager.getImageFileNames();
		for(String imageName : imageNames) {
			//testReverseSampling(imageName, sourceResolution, 500);
		}
	}
	
	private void exportImage(ImageData imageData, String imageName, String exportDirectory) {
		byte [] encoding = imageData.asEncodedImage(ImageData.FORMAT_WSQ);
		String baseName = StringUtils.concat(imageName, imageData.getWidth(), imageData.getHeight());
		String exportFile = FileUtils.expandConfigFile(exportDirectory, baseName, ImageData.FORMAT_WSQ);
		try {
			FileUtils.byteArrayToFile(encoding, exportFile, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	/*
	static void testReverseSampling(String imageName, int sourceResolution, int targetResolution) {
		ImageData imageData = TestDataManager.loadImage(imageName);
		imageData.setResolution(sourceResolution);

		BufferedImage srcImage = ABISClientUtils.asBufferedImage(imageData);

		// Scale forward
		ABISClientUtils.rescaleResolution(imageData, targetResolution);
		
		// Scale back
		ABISClientUtils.rescaleResolution(imageData, sourceResolution);
		BufferedImage recoverImage = ABISClientUtils.asBufferedImage(imageData);
		
		double distance = ImageUtils.getImageDistance(srcImage, recoverImage);
		printResult("Distance internal " + imageName, distance);
		
		double scaleForward = (double) targetResolution/ sourceResolution;
		double scaleReverse = 1/ scaleForward; //sourceResolution/targetResolution;
		
		BufferedImage scaleImage2 = ImageUtils.scaleImage(srcImage, scaleForward);
		BufferedImage recoverImage2 = ImageUtils.scaleImage(scaleImage2, scaleReverse);
		double distance2 = ImageUtils.getImageDistance(srcImage, recoverImage2);
		printResult("Distance Java 2D " + imageName, distance2);
	}
	*/

	/*
	private static ImageData loadResampledImage1(String imageName, int sourceResolution, int targetResolution) {
		// Default imageDate incorrectly assigned at 500 dpi
		ImageData imageData = TestDataManager.loadImage(imageName);
		
		// Set resolution to correct value
		imageData.setResolution(sourceResolution);
		
		// Rescale the image to target
		ABISClientUtils.rescaleResolution(imageData, targetResolution);
		
		return imageData;
	}

	private static ImageData loadResampledImage2(String imageName, int sourceResolution, int targetResolution) {
		try {
			String fileName = TestDataManager.getImageFile(imageName);
			BufferedImage bufferedImage = ImageUtils.bufferedImageFromFile(fileName);
			// Easy constructor to generate from BufferedImage with specified source and target resolutions
			ImageData imageData = new ImageData();
			ABISClientUtils.fromBufferedImage(imageData, bufferedImage, sourceResolution);
			imageData.rescaleImage(targetResolution);
			return  imageData;
		} catch (Exception e) {
			handleException(e);
			return null;
		}
	}
	*/

}
