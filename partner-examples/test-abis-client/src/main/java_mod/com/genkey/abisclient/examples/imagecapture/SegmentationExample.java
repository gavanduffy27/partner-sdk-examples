package com.genkey.abisclient.examples.imagecapture;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.genkey.abisclient.Enums.GKSwitchParameter;
import com.genkey.abisclient.ErrorStatusCodes;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.SegmentationRegion;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.StopWatch;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;

public class SegmentationExample extends ExampleModule {

    static String ThumbsFile = "Thumbs_01";
    static String TwoFingerFile = "twoFinger";
    static String MixedFingerFile = "mixedFinger";

    boolean displayImages=true;

    
    public static void main(String [] args) {
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


	/*
	private static void migrateImages() {
		String [] formats = {ImageUtils.EXT_BMP, ImageUtils.EXT_PGM};
		String testDir = FileUtils.expandConfigPath("test/fesdk");
		String [] fileNames = FileUtils.getFilenames(testDir, true);
		for(String fileName : fileNames) {
			try {
				//BufferedImage image = ImageUtils.bufferedImageFromFile(fileName);
				for (String format : formats) {
					String tgtFile = FileUtils.forceExtension(fileName, format);
					ImageUtils.bufferedImageToFile(image, tgtFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	*/
	
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
        doSingleImageExample(ThumbsFile, new int[] {6,1});
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });

        printHeader("Testing Full Extract asynchronous/mt");
        setFullExtract(true, true);
        doSingleImageExample(ThumbsFile, new int[] { 6, 1 });
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });

        printHeader("Testing Quality only synchronous/mt");
        setQualityOnly(false, true);
        doSingleImageExample(ThumbsFile, new int[] { 6, 1 });
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });

        printHeader("Testing Quality only asynchronous/mt");
        setQualityOnly(true, true);
        doSingleImageExample(ThumbsFile, new int[] { 6, 1 }); 
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });

        printHeader("Testing Fast Quality asynchronous/mt");
        setFastQuality(true, true);
        doSingleImageExample(ThumbsFile, new int[] { 6, 1 });
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });

        printHeader("Testing segmentation only");
        setSegmentationOnly();
        doSingleImageExample(ThumbsFile, new int[] { 6, 1 });
        doSingleImageExample(TwoFingerFile, new int[] { 8, 7 });
        
	}


	public void fourFingerExamples() {
		TestDataManager.setImageDirectory(FourFingerDirectory);
		int [] fingers = Commons.generateRangeV(8,7,2,3);
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
        // If set to false (i.e. synchronous) the processing occurs within constructor along with segmentation.
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

	/** 
	 * Processes a multiple finger issues ..
	 * @param imageFile
	 * @param fingers
	 */
	private void doSingleImageExample(String imageFile, int[] fingers) {
        printHeader("Analyzing new image " + imageFile,'-');
        ImageData image = TestDataManager.loadImage(imageFile);
        StopWatch timer = new StopWatch();
        ImageContext context = new ImageContext(image, fingers);
        
        int [] detected = context.getDetectedFingers();
        printResult("Detected fingers", Commons.arrayToList(detected));
        int[] assigned = context.getFingers();
        
        
        // check if fingers detected were consistent
        if (! context.isFingersConsistent()) {
        	int [] fingersDetected = context.getDetectedFingers();
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
        if ( ! checkErrorHandler(context)) {
        	return;
        }
        
        printTimerResult("Context create", timer);

        List<ImageData> imageSegments = CollectionUtils.newList();
        // Obtain segments
        for (int ix = 0; ix < context.count(); ix++)
        {
            SegmentationRegion segment = context.getSegmentationRegion(ix);
            ImageData imageSegment = context.extractImageSegment(ix, true);
            imageSegments.add(imageSegment);
            printObject("Segment[" + ix + "]", segment);
        }
        printTimerResult("Segmentation", timer);

        
        // Now check quality assessment
        int totalScore = 0;
        for (int ix = 0; ix < context.count(); ix++)
        {
            QualityInfo qInfo = context.getQualityInfo(ix);
            totalScore += qInfo.getQualityScore();
            printTimerResult("Quality segment accessed", ix, timer);
        }
        // Save image context and capture next fingers

        // Later ...
        printHeader("Reference Extraction");
        StopWatch refTimer = new StopWatch();
        for (int ix = 0; ix < context.count(); ix++)
        {
            ReferenceDataItem reference = context.getReferenceData(ix);
            printTimerResult("Reference", ix, refTimer);
        }
        printTimerResult("Reference extraction complete", refTimer);
		
        processForBiometricProfile(context);
        
        context.dispose();
        
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
		 * @return
		 */
		public byte [] getReferenceEncoding() {
			return fingerTemplate.getReferenceData();
		}
		
		/**
		 * Return the binary encoding for the image data
		 * @return
		 */
		public byte [] getImageEncoding() {
			return fingerImage.asEncodedImage(ImageData.FORMAT_WSQ);
		}

		
		
	}
	
	/**
	 * Mock class container for biometric data that goes into the BiometricReferenceProfile
	 * 
	 * @author gavan
	 *
	 */
	static class BiometricReferenceProfile extends HashMap<Integer, ProfileDataItem> {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Add profile information for specified finger
		 * @param fingerIndex
		 * @param reference
		 * @param image
		 */
		public void addFinger(int fingerIndex, ReferenceDataItem reference, ImageData image) {
			this.put(fingerIndex, new ProfileDataItem(fingerIndex, reference, image));
		}
		
		/**
		 * Return the fingers present in the container
		 * @return
		 */
		public Set<Integer> getFingers() {
			return this.keySet();
		} 
		
		/**
		 * Return the profileData for specified finger
		 * @param fingerIndex
		 * @return
		 */
		public ProfileDataItem getProfileDataForFinger(int fingerIndex) {
			return get(fingerIndex);
		}
		
		/**
		 * Return reference encoding for specified finger
		 * @param finger
		 * @return
		 */
		public byte [] getReferenceEncoding(int finger) {
			return getProfileDataForFinger(finger).getReferenceEncoding();
		}
		
		/**
		 * Return image encoding for specified finger
		 * @param finger
		 * @return
		 */
		public byte [] getImageEncoding(int finger) {
			return getProfileDataForFinger(finger).getImageEncoding();
		}
		
	}
	
	private void processForBiometricProfile(ImageContext context) {
		List<ImageData> segments = new ArrayList<>();
		List<ReferenceDataItem> templates = new ArrayList<>();
		
		List<ImageData> imageDisplayList = new ArrayList<>();
		
		int [] fingers = context.getFingers();
		
		BiometricReferenceProfile profile = new BiometricReferenceProfile();
		
		for(int ix=0; ix < context.count(); ix++) {
			ReferenceDataItem referenceItem= context.getReferenceData(ix);
			ImageData imageData = context.extractImageSegment(ix, true);
			int fingerIndex = fingers[ix];
			profile.addFinger(fingerIndex, referenceItem, imageData);
			imageDisplayList.add(imageData);
		}
		
		if (this.isDisplayImages()) {
			super.showImageList(imageDisplayList, "ImageSegments", 0.25);
			//this.getUserMessageHandler().showImageList("Image Segments"), imageDisplayList, 0, 0, 0.251);
		}
	}

	private boolean checkErrorHandler(ImageContext context) {
		long status = context.getStatus();

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
			if ( ! promptOperatorContinue(errorDescription)  ) {
				return false;
			}
		}
		
		// Does this condition preclude further processing
		boolean isBlock = ErrorStatusCodes.isBlockingError(status);
		
		return ! isBlock;
	}

	boolean checkImageStatus(ImageContext context) {
        long status = context.getStatus();  
        if (status < 0) {
        	//String message = ImageContextSDK.getErrorDescription(status);
        }
        return false;
	}
	
}
