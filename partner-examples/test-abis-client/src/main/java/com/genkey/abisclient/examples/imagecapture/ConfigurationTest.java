package com.genkey.abisclient.examples.imagecapture;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.ABISClientException;
import com.genkey.abisclient.EnrolmentSettings;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.ErrorStatusCodes;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.platform.utils.FormatUtils;

public class ConfigurationTest extends ExampleModule {

	@Override
	protected void runAllExamples() {
		testExceptionCatch();
		showErrorCodes();
		enrollmentSettingTest();
	}

	
	public void testExceptionCatch() {
		try {
			
			throw new ABISClientException("External error description", (int)ErrorStatusCodes.BORDER_INFRINGEMENT_ERROR);
			
		} catch (ABISClientException e) {
			String desc = e.getDescription();
			String shortName = e.getShortName();
			String message = e.getMessage();
			printResult(shortName, message);
		}
	}
	
	public void showErrorCodes() {
		List<Integer> errorCodes = ImageContextSDK.getAllErrorCodes();
		List<Integer> blockingCodes = new ArrayList<>();
		List<Integer> warningCodes = new ArrayList<>();
		List<Integer> otherCodes = new ArrayList<>();
		
		
		for(int errorCode : errorCodes) {
			if (ErrorStatusCodes.isBlockingError(errorCode)) {
				blockingCodes.add(errorCode);
			} else if (ErrorStatusCodes.isWarning(errorCode)) {
				warningCodes.add(errorCode);
			} else {
				otherCodes.add(errorCode);
			}
		}

		showErrorCodes("Blocking", blockingCodes);
		showErrorCodes("Warning", warningCodes);
		showErrorCodes("Other", otherCodes);
	
	}

	private void showErrorCodes(String header, List<Integer> errorCodes) {
		FormatUtils.printHeader(header);
		for(int errorCode : errorCodes) {
			String description = ErrorStatusCodes.getDescription(errorCode);
			FormatUtils.printResult(String.valueOf(errorCode), description);
		}
	}

	public void enrollmentSettingTest() {
		EnrolmentSettings bookMark = new EnrolmentSettings();
		EnrolmentSettings settings =  new EnrolmentSettings();
		
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
			
		//Rstore from bookmark
		bookMark.applySettings();
		
	}
	
}
