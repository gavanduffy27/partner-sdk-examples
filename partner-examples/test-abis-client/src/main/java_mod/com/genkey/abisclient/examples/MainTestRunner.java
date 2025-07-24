package com.genkey.abisclient.examples;


import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;

import com.genkey.abisclient.examples.imagecapture.*;
import com.genkey.abisclient.examples.matchengine.MatchEngineSuite;
import com.genkey.abisclient.examples.transport.TransportTestSuite;
import com.genkey.abisclient.examples.verify.VerifyTestClass;
import com.genkey.abisclient.verification.VerificationSDK;

import com.genkey.platform.utils.ArgumentMap;
import com.genkey.platform.utils.GKRuntimeException;
import com.genkey.platform.utils.PropertyMap;

public class MainTestRunner {

	private static boolean runDebugTests=true;
	
	final static String DefaultHardwareId="74c690deb9a1d259";
	public final static String ARG_HARDWARE_ID="hardwareId";
	public final static String ARG_CONFIG_DIRECTORY="configDirectory";
	public final static String ARG_SECURITY_DOMAIN="securityDomain";
	public final static String ARG_SERVER_DOMAIN="serverEmulationDomain";
	public final static String ARG_USE_LEGACY="useLegacy";
	public final static String ARG_ENABLE_BC="enableBC";
	public final static String ARG_USE_STANDARD_CONFIG="useStandardConfig";
	public final static String ARG_EMBEDDED="embedded";
	
	final static String ARG_MESSAGE_HANDLER="messageHandler";
	

	private static final String ARG_USE_GUI = "allowGui";

	public static void main(String [] args) {
		mainRunner(args);
	}
	
	public static void mainRunner(String [] args) {
		parseArgs(args);
		if (runDebugTests) {
			debugTests();
		} else {
			fullTests();
		}
		ExampleModule.PrintMessage("Proceeding to shutdown");
		ImageContextSDK.shutdownLibrary();
		ExampleModule.PrintMessage("All tests complete!");
	}

	public static String DefaultLoadFile="./abisClient.ini";
	
	private static void parseArgs(String[] args) {
		ArgumentMap argMap = new ArgumentMap(DefaultLoadFile, args);
				
		initializeLibrary(argMap);
		initializeGUI(argMap);
		PropertyMap props = argMap.getNamedArguments();
		ExampleModule.getPropertyMap().importMap(props);
		
	}
	
	public static void mainStubCall( ) {
		System.out.println("Java library call");
	}

	
	private static void initializeLibrary(ArgumentMap argMap) {
		if (!ABISClientLibrary.isInitialized()) {
			String hardwareId = argMap.getArgument(ARG_HARDWARE_ID, DefaultHardwareId);
			String configDirectory = argMap.getArgument(ARG_CONFIG_DIRECTORY);
			boolean useEmbedded = argMap.getArgumentBoolean(ARG_EMBEDDED, false);
			boolean useLegacy = argMap.getArgumentBoolean(ARG_USE_LEGACY, false);
			boolean enableBC = argMap.getArgumentBoolean(ARG_ENABLE_BC, false);
			
			int securityDomain = argMap.getArgumentInt(ARG_SECURITY_DOMAIN, -1);
			int bskCode = argMap.getArgumentInt(ARG_SERVER_DOMAIN, -1);
	//		ABISClientLibrary.setLoggingConfig(true, false);
			ABISClientLibrary.init(hardwareId, useEmbedded, useLegacy, configDirectory, securityDomain);
	//		ABISClientLibrary.setLoggingConfig(true, false);
			int secureDomain = VerificationSDK.getSecurityDomain();
			VerificationSDK.setSecurityDomain(securityDomain);
			ImageContextSDK.setThreshold(GKThresholdParameter.ServerEmulationDomain, bskCode);	
			
			if (!useLegacy && enableBC) {
				ImageContextSDK.enableBackwardsCompatibility();
			}
		}
		int securityDomain = argMap.getArgumentInt(ARG_SECURITY_DOMAIN, -1);
		if(securityDomain != -1) {
			VerificationSDK.setSecurityDomain(securityDomain);
		}
		boolean useStandardConfig = argMap.getArgumentBoolean(ARG_USE_STANDARD_CONFIG, true);
		MatchEngineConfiguration meConfig = MatchEngineConfiguration.getInstance();
		meConfig.setUseStandardConfig(useStandardConfig);
	}

	private static void initializeGUI(ArgumentMap argMap) {
		boolean useGui = argMap.getArgumentBoolean(ARG_USE_GUI, false);
		String messageHandlerClass =argMap.getArgument(ARG_MESSAGE_HANDLER);
		UserMessageHandler handler=null;
		if (messageHandlerClass != null) {
			try {
				handler = (UserMessageHandler) Class.forName(messageHandlerClass).newInstance();
				
			} catch (Exception e) {
				throw new GKRuntimeException("Unable to instantiate message handler " + messageHandlerClass);
			}
		} else {
			handler = new DefaultUserMessageHandler(useGui);
		}		
	}

	public static void fullTests() {
		ABISClientExampleSuite fullSuite = new ABISClientExampleSuite();
		fullSuite.runTestExamples();
		ExampleModule.PrintMessage("All tasks complete");
	}
	
	
	public static void matchEngineTests() {
		MatchEngineSuite testSuite = new MatchEngineSuite();
		testSuite.runTestExamples();
	}

	public static void transportTests() {
		TransportTestSuite transportSuite = new TransportTestSuite();
		transportSuite.runTestExamples();
	}

	public static void miscTests() {
		
		VerifyTestClass verifyTestClass = new VerifyTestClass();
		verifyTestClass.runSelectedExamples();

		InkSegmentExamples inkTests = new InkSegmentExamples();
		//inkTests.runTestExamples();
		
		LogDiagnosticTest logTests = new LogDiagnosticTest();
		///logTests.runTestExamples();
		
		return;

		/*

		DiagnosticTest diagnosticTest = new DiagnosticTest();
		diagnosticTest.runTestExamples();
		
		BackwardCompatibilityTest bcTests = new BackwardCompatibilityTest();
		//bcTests.runTestExamples();
		
		ConfigurationTest configTest = new ConfigurationTest();
		configTest.runTestExamples();	


		ImageDecodingTest decodingTest = new ImageDecodingTest();
		decodingTest.runTestExamples();
		VerifyTestClass verifyTestClass = new VerifyTestClass();
		verifyTestClass.runSelectedExamples();
		*/

	}

	public static void imageCaptureTests() {
		ImageCaptureSuite imageSuite = new ImageCaptureSuite();
		imageSuite.runTestExamples();
	}
	
	public static void badSequenceTest() {
		VerifyTestClass verifyTestClass = new VerifyTestClass();
//		verifyTestClass.runSelectedExamples();
		miscTests();
		SegmentationExample segExample = new SegmentationExample();
		segExample.runTestExamples();
	}

	public static void allTests() {
		miscTests();
		imageCaptureTests();
		transportTests();
		matchEngineTests();
	}

	public static void debugTests() {
		miscTests();
	}
}
