package com.genkey.abisclient.examples.imagecapture;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.Enums.GKSwitchParameter;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.StopWatch;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.ImageUtils;

public class SingleFingerCapture extends ExampleModule{
	

	public static void main(String [] args) {
		SingleFingerCapture test = new SingleFingerCapture ();
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
		testMultipleSampleConsolidate();
		doSingleFingerMultipleImage();
		doQualityReferenceTest();
	}

	/**
	 * 
	 */
	
	public void doSingleFingerMultipleImage() {
		doSingleFingerMultipleImage(false);
		doSingleFingerMultipleImage(true);
	}

	/**
	 * Example scenario that emulates examination of multiple images from a single presentation.
	 * Quality assessment is performed on each image, but reference is only extracted from the best image.
	 * @param useFastQuality	If set to true then use fast(er) rather than standard quality assessor
	 */
	public void doSingleFingerMultipleImage(boolean useFastQuality) {
		ImageContextSDK.resetConfiguration();
		ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.FastQualityAssessor, useFastQuality);
		
		int finger=3;
		int subject=1;
		int [] samples = TestDataManager.generateArray(1,5);
		List<ImageData> imageList = TestDataManager.loadImages(subject, finger, samples);

		List<ImageContext> contextList = new ArrayList<ImageContext>();
		ImageContext bestContext = null;
		QualityInfo bestQuality = null;
		int index=0;
		for(ImageData image : imageList) {
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
		
		for(ImageContext cxt : contextList) {
			cxt.dispose();
		}
	}

	public void testMultipleSampleConsolidate() {
		int [] samples = Commons.generateRangeArray(1, 3);
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
		ImageData image = TestDataManager.loadImage(1,3,1);
//		StopWatch timer = new StopWatch();
		ImageContext context = new ImageContext(image, 3);
//		context.dispose();
	}
	
	public void doQualityReferenceTest() {
		ImageContextSDK.resetConfiguration();
		ImageContextSDK.setSwitch(GKSwitchParameter.QualityAssessOnly, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, false);
		ImageData image = TestDataManager.loadImage(1,3,1);
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
	
}
