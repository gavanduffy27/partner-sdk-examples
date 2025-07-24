package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.examples.ExampleSuite;

public class ImageCaptureSuite extends ExampleSuite {

	@Override
	protected void initExampleSuite() {
		this.addExample(new SingleFingerCapture());
		this.addExample(new DuplicateDetectionTest());
		this.addExample(new SegmentationExample());
	}

}
