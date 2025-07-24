package com.genkey.abisclient.examples.misc;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.ABISClientLibrary;

public class AdHocTest extends ExampleModule{

	@Override
	protected void runAllExamples() {
		testSDKProperties();
		showLibraryDependencies();
	}

	public void testSDKProperties() {
		String appName = ABISClientLibrary.getAndroidApplicationName();
		super.printResult(ABISClientLibrary.PARAM_ANDROID_APPNAME, appName);
		ABISClientLibrary.setAndroidApplicationName("com.genkey.product-1");
		appName = ABISClientLibrary.getAndroidApplicationName();
		super.printResult(ABISClientLibrary.PARAM_ANDROID_APPNAME, appName);
	}

	public void showLibraryDependencies() {
		for (String libraryName : ABISClientLibrary.androidLibraries) {
			super.printMessage(libraryName);
		}
	}

	public static void main(String [] args) {
		AdHocTest example = new AdHocTest();
		example.runTestExamples();
	}
	
}
