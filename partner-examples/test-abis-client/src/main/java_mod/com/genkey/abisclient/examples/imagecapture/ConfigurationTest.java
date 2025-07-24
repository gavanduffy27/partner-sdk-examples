package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.EnrolmentSettings;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.examples.ExampleModule;

public class ConfigurationTest extends ExampleModule {

	@Override
	protected void runAllExamples() {
		enrollmentSettingTest();
	}

	public void enrollmentSettingTest() {
		EnrolmentSettings settings = new EnrolmentSettings();
		
		settings.syncLoad();
		String configInit = settings.toString();
		String switchInit = settings.getSwitchSettings().toString();
		String thresholdInit = settings.getThresholdSettings().toString();

		
		super.printHeaderResult("Settings", settings);
		EnrolmentSettings.Switch switchSettings = settings.getSwitchSettings();
		EnrolmentSettings.Threshold thresholds = settings.getThresholdSettings();
		
		super.printHeaderResult("Switches", switchSettings);
		super.printHeaderResult("Thresholds", thresholds);
		
		switchSettings.AutoExtractReference=true;
		switchSettings.AutoQualityAssess=true;
		super.printHeaderResult("Switches", switchSettings);
		
		
		settings.applySettings();

		super.printHeaderResult("Settings", settings);
		
		thresholds.SecurityDomain=71;
		thresholds.ServerEmulationDomain=1;
		thresholds.HandDropMax=52;
		thresholds.FingerShortFallMin=100;
		thresholds.applyThresholds();
		super.printHeaderResult("Settings", settings);
		super.printHeaderResult("Thresholds", thresholds);
		
		settings.syncLoad();
		super.printHeaderResult("Settings", settings);
		
		thresholds.syncLoad();
		super.printHeaderResult("Thresholds", thresholds);
		
		int emulationDomain = (int) ImageContextSDK.getThreshold(GKThresholdParameter.ServerEmulationDomain);
		int securityDomain = (int) ImageContextSDK.getThreshold(GKThresholdParameter.SecurityDomain);
		double handDropMax = ImageContextSDK.getThreshold(GKThresholdParameter.HandDropMax);
		int fingerSHortFall = (int) ImageContextSDK.getThreshold(GKThresholdParameter.FingerShortFallMin);
		
		printResult("emulationDomain", emulationDomain);
		printResult("securityDomain", securityDomain);
		
		switchSettings.fromString(switchInit);
		super.printHeaderResult("Switches", switchSettings);
		
		thresholds.fromString(thresholdInit);
		super.printHeaderResult("Thresholds", thresholds);
		
		settings.fromString(configInit);
		super.printHeaderResult("Settings", settings);
			
		
		
	}
	
}
