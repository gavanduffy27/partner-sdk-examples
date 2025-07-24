package com.genkey.abisclient.examples.imagecapture;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageContextSDK.MatchDetails;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.platform.utils.ImageUtils;

public class DuplicateDetectionTest extends ExampleModule {

	
	
	@Override
	protected void setUp() {
		super.setUp();
		TestDataManager.setImageDirectory(ExampleModule.SingleFingerDirectory);
		TestDataManager.setImageFormat(ImageUtils.EXT_BMP);
	}

	@Override
	protected void runAllExamples() {
		duplicateDetectionTest();
	}

	public void duplicateDetectionTest() {
		int [] fingers = ExampleModule.FourFingers;
		
		
		
		int subject=1;
		int sample=1;

		// Simulates an enrolment where each new enrolled references is tested for a match
		// against existing references
		ImageContextSDK.MatchDetails matchDetails = new MatchDetails();
		List<ReferenceDataItem> referenceList = new ArrayList<ReferenceDataItem>();
		for(int ix=0; ix < fingers.length; ix++) {
			String imageFile = TestDataManager.getImageFile(subject, fingers[ix], sample);
			ReferenceDataItem reference = TestDataManager.loadReference(imageFile, fingers[ix]);
			boolean isDuplicate = ImageContextSDK.isDuplicate(reference, referenceList,  matchDetails);
			if (isDuplicate) {
				printMessage("Unexpected match of reference[" + ix + "] with reference[" 
								+ matchDetails.getIndex() + "] with score = " + matchDetails.getScore());
			} else {
				printMessage("Expected failure to match with score = " + matchDetails.getScore());
				referenceList.add(reference);
			}
			// Add this one to accumulated list;
		}
		
		// Now try with real duplicates. Same process but with threshold set explicitly
		sample=2;
		for(int ix=0; ix < fingers.length; ix++) {
			String imageFile = TestDataManager.getImageFile(subject, fingers[ix], sample);
			ReferenceDataItem reference = TestDataManager.loadReference(imageFile, fingers[ix]);
			// This call we specify the threshold match explicitly
			boolean isDuplicate = ImageContextSDK.isDuplicate(reference, referenceList,  0.2f, matchDetails);
			if (isDuplicate) {
				printMessage("Expected match of reference[" + ix + "] with reference[" 
						+ matchDetails.getIndex() + "] with score = " + matchDetails.getScore());
			} else {
				printMessage("Unexpected match failure of reference[" + ix + "] with reference[" 
						+ matchDetails.getIndex() + "] with best score = " + matchDetails.getScore());
				referenceList.add(reference);
			}
		}
		
	}
}
