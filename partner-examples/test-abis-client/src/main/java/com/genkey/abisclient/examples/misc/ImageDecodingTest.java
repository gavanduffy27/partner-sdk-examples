package com.genkey.abisclient.examples.misc;


import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.FileUtils;

/**
 * Tests use of ImageData class for importing export data from difference formats
 */
public class ImageDecodingTest extends ExampleModule {


	protected void setUp() {
		super.setUp();
//		TestDataManager.setImageDirectory(SingleFingerDirectory);
//		TestDataManager.setImageFormat(ImageUtils.EXT_BMP);
	}
	
	@Override
	protected void runAllExamples() {
		convertImages();
		testImageEncoding();
	}

	public void convertImages() {
//		convertImages(SingleFingerDirectory,"bmp", SingleFingerDirectory, "pgm");
//		convertImages(TwoFingerDirectory,"bmp", TwoFingerDirectory, "pgm");
	}
	
	public static void convertImages(String sourceDir, String sourceFormat, String targetDir, String targetFormat) {
		try {
			String sourcePath = FileUtils.expandConfigPath(sourceDir);
			String [] sourceFiles = FileUtils.getFilenames(sourcePath, sourceFormat, true);
			for(String sourceFile : sourceFiles) {
				String shortName = FileUtils.shortName(sourceFile);
				String tgtShortName = FileUtils.forceExtension(shortName, targetFormat);
				String targetFile = FileUtils.expandConfigFile(targetDir, tgtShortName , targetFormat);
				byte [] srcEncoding = FileUtils.byteArrayFromFile(sourceFile);
				ImageData image = new ImageData(srcEncoding, sourceFormat);
				byte [] tgtEncoding = image.asEncodedImage(targetFormat);
				FileUtils.byteArrayToFile(tgtEncoding, targetFile);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}
	
	static String [] testFormats={ "pgm", "bmp", "wsq","jp2"};
	
	public void testImageEncoding() {
		String imageFile = TestDataManager.getImageFile(1, 3, 1);
		ImageData image = TestDataManager.loadImage(imageFile);
		byte [] data = image.getPixelData();

//		testImageEncoding(image, imageFile, "wsq");
		
		
		for(String format : testFormats) {
			testImageEncoding(image, imageFile, format);			
		}
	}

	public void testImageEncoding(ImageData imageData, String fileName,
			String format) {
		try {
			// Encode image
			printHeader("Test image encoding for " + fileName + " using " + format);
			String outputDirectory = FileUtils.expandConfigPath("images/testEncoding");
			FileUtils.checkPathExists(outputDirectory);
			//ImageData imageData = new ImageData(image);
			printMessage("Encoding to target format " + format);
			byte[] encoding = imageData.asEncodedImage(format);
			
			//	byte[] encodinga = imageData.asEncodedImage("bmp");
			
			String baseName = FileUtils.shortName(fileName);
			baseName = FileUtils.forceExtension(baseName, format);
			String tgtFile = FileUtils.expandConfigFile(outputDirectory, baseName, format);
			printMessage("Encoding to target file " + tgtFile);
			FileUtils.byteArrayToFile(encoding, tgtFile);
			
			// reconstruct image
			printMessage("Reconstructing from " + format);
			ImageData imageData2 = new ImageData(encoding, format);
			byte[] encoding2 = imageData2.asEncodedImage("bmp");
			
			String tgtFile2 = FileUtils.applyTimestamp(tgtFile);
			tgtFile2 = FileUtils.forceExtension(tgtFile2, "bmp");
			FileUtils.byteArrayToFile(encoding2, tgtFile2);
			
			
		} catch (Exception e) {
			handleException(e);
		}

	}

}
