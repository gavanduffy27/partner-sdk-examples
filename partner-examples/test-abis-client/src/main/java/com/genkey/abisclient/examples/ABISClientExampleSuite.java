package com.genkey.abisclient.examples;

import com.genkey.abisclient.examples.imagecapture.ConfigurationTest;
import com.genkey.abisclient.examples.imagecapture.ImageCaptureSuite;
import com.genkey.abisclient.examples.matchengine.MatchEngineSuite;
import com.genkey.abisclient.examples.misc.ImageDecodingTest;
import com.genkey.abisclient.examples.transport.TransportTestSuite;
import com.genkey.abisclient.examples.verify.VerifyTestClass;

public class ABISClientExampleSuite extends ExampleSuite{
	

	// More to come in later releases
	@Override
	protected void initExampleSuite() {
		this.addExample(new ImageCaptureSuite());
		this.addExample(new ImageDecodingTest());
		this.addExample(new MatchEngineSuite());
		this.addExample(new TransportTestSuite());
		this.addExample(new VerifyTestClass());
		this.addExample(new ConfigurationTest());
		
	}

}
