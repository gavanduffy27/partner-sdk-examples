package com.genkey.abisclient.examples;


import java.util.Map;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.SystemSettings;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.abisclient.examples.bordercontrol.PassportExample;
import com.genkey.abisclient.examples.imagecapture.*;
import com.genkey.abisclient.examples.matchengine.MatchEngineSuite;
import com.genkey.abisclient.examples.misc.AdHocTest;
import com.genkey.abisclient.examples.misc.HajoReplayTest;
import com.genkey.abisclient.examples.transport.FingerDecodingExample;
import com.genkey.abisclient.examples.transport.FingerEnrollmentExample;
import com.genkey.abisclient.examples.transport.SmartContainerTest;
import com.genkey.abisclient.examples.transport.SubjectEnrollmentExample;
import com.genkey.abisclient.examples.transport.TransportTestSuite;
import com.genkey.abisclient.examples.utils.ExampleSettings;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.InternalSettings;
import com.genkey.abisclient.examples.verify.BackwardCompatibilityTest;
import com.genkey.abisclient.examples.verify.VerifyTestClass;
import com.genkey.abisclient.verification.AnonymousReferenceMatcher;
import com.genkey.abisclient.verification.VerificationSDK;
import com.genkey.platform.utils.AndroidPlatform;
import com.genkey.platform.utils.ArgumentMap;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.GKRuntimeException;
import com.genkey.platform.utils.PropertyMap;
import com.genkey.platform.utils.StringUtils;

public class MainTestRunner extends BaseTestRunner{

	private static boolean runDebugTests=true;
	

	

	public static void main(String [] args) {
		mainRunner(args);
	}
	
	public static void mainRunner(String [] args) {
		parseArgs(args);
		ExampleSettings settings = ExampleSettings.getInstance();
		if (settings.isStartupTest()) {
			runStartupTests();
		} else if (settings.isLicenseTest()) {
			simpleInstallTests();
		} else if (settings.isRunBenchmark()) {
			MainTestRunner.runBenchmarkTests();
		} else if (settings.isRunFullTests()) {
			fullTests();
		} else {
			runSelectedTests();			
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
		String configurationPath = ImageContextSDK.getConfigurationPath();
		FileUtils.setConfigurationPath(configurationPath);
	}
	
	public static void mainStubCall( ) {
		System.out.println("Java library call");
	}

		
	private static void showSystemSettings() {
		Map<String, String> librarySettings = SystemSettings.getAllProperties();
		FormatUtils.printObject("Library Settings", CollectionUtils.mapToString(librarySettings,"\n"));	
		
		FormatUtils.printObject("Example settings", ExampleSettings.getInstance());
		FormatUtils.printObject("Internal settings", InternalSettings.getInstance());
		
	}

	private static void initializeLibrary(ArgumentMap argMap) {
		
		if (!ABISClientLibrary.isInitialized()) {

			// For Java we need to load libraries with no initialization
			ABISClientLibrary.loadLibraries();
			
			ExampleSettings settings = ExampleSettings.getInstance();
			
			//ABISClientLibrary.initializeDefault();
			
			settings.initialize();
			
			settings.runSystemSettingExamples();
			
			SubjectEnrollmentReference.setFingerDataSize(35);
			
			ABISClientLibrary.initializeDefault();
			
			
			/*
			String hardwareId = argMap.getArgument(ARG_HARDWARE_ID, DefaultHardwareId);
			String configDirectory = argMap.getArgument(ARG_CONFIG_DIRECTORY);
			boolean defaultEmbedded = AndroidPlatform.isAndroidPlatform();
			boolean useEmbedded = argMap.getArgumentBoolean(ARG_EMBEDDED, defaultEmbedded);
			boolean useLegacy = argMap.getArgumentBoolean(ARG_USE_LEGACY, false);
			boolean enableBC = argMap.getArgumentBoolean(ARG_ENABLE_BC, false);
			
			int securityDomain = argMap.getArgumentInt(ARG_SECURITY_DOMAIN, -1);
			int bskCode = argMap.getArgumentInt(ARG_SERVER_DOMAIN, -1);
	//		ABISClientLibrary.setLoggingConfig(true, false);
			ABISClientLibrary.setAndroidPlatform(useEmbedded);
			*/
			
			
			ABISClientLibrary.initializeDefault();
			
			/*	
			if (settings.is) {
				ABISClientLibrary.initAndroid(hardwareId, false);
			} else {
				ABISClientLibrary.init(useLegacy, true);
			}
			
			ABISClientLibrary.setConfigurationPath(configDirectory);
			//ABISClientLibrary.setSecurityDomain(securityDomain);
			VerificationSDK.setSecurityDomain(securityDomain);
			
			boolean useLog10Far = VerificationSDK.isUseFAR10ScoreScheme();
			if (! useLog10Far ) {
				AnonymousReferenceMatcher.setUseFAR10ScoreScheme(true);
				useLog10Far = AnonymousReferenceMatcher.isUseFAR10ScoreScheme();
			}
			
			ImageContextSDK.setThreshold(GKThresholdParameter.ServerEmulationDomain, bskCode);	
			if (!useLegacy && enableBC) {
				ImageContextSDK.enableBackwardsCompatibility();
			}
			 */

		}
		/*
		int securityDomain = argMap.getArgumentInt(ARG_SECURITY_DOMAIN, -1);
		if(securityDomain != -1) {
			VerificationSDK.setSecurityDomain(securityDomain);
		}
		boolean useStandardConfig = argMap.getArgumentBoolean(ARG_USE_STANDARD_CONFIG, true);
		MatchEngineConfiguration meConfig = MatchEngineConfiguration.getInstance();
		meConfig.setUseStandardConfig(useStandardConfig);
		*/
		MatchEngineConfiguration meConfig = MatchEngineConfiguration.getInstance();
		boolean useStandardConfig = meConfig.isUseStandardConfig();
		if (!useStandardConfig) {
			meConfig.setUseStandardConfig(true);
		}

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
		
		HajoReplayTest replayTest = new HajoReplayTest();
		replayTest.runTestExamples();
		
		AdHocTest adHocTest = new AdHocTest();
//		adHocTest.runTestExamples();
		
		VerifyTestClass verifyTestClass = new VerifyTestClass();
//		verifyTestClass.runSelectedExamples();

		InkSegmentExamples inkTests = new InkSegmentExamples();
//		inkTests.runTestExamples();
		
		LogDiagnosticTest logTests = new LogDiagnosticTest();
//		logTests.runTestExamples();
		
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

	public static void runStartupTests() {
		FormatUtils.printBanner("Startup test only");
	}
	
	public static void simpleInstallTests() {
        SingleFingerCapture test = new SingleFingerCapture();
        test.runTestExamples();		
	}
	
	public static void runBenchmarkTests() {
        ExampleSettings settings = ExampleSettings.getInstance();

        String fileName =  settings.getTestImage(ExampleSettings.BenchMarkImage);

        SegmentationExample example = new SegmentationExample();
        
        example.twoFingerExample(fileName, new int[] {8, 7} );
		
	}
	
	public static void runSelectedTests() {
		
		FingerDecodingExample fingerExample = new FingerDecodingExample();
		fingerExample.runTestExamples();
		
		
		AdHocTest adHocTest = new AdHocTest();
		adHocTest.runTestExamples();
		
		
		PassportExample passExample = new PassportExample();
		passExample.runTestExamples();
		
		/*
		SubjectEnrollmentExample sEnrolTest = new SubjectEnrollmentExample();
		sEnrolTest.runTestExamples();
		
		ConfigurationTest configTest = new ConfigurationTest();
//		configTest.enrollmentSettingTest();
		
		SmartContainerTest containerTest = new SmartContainerTest();
		containerTest.runTestExamples();

		SingleFingerCapture singleFingerExample = new SingleFingerCapture();
//		singleFingerExample.imageRotateTest();
		*/
		
		/*
		VerifyTestClass verifyTest = new VerifyTestClass();
		verifyTest.runTestExamples();
		
		SegmentationExample segExample = new SegmentationExample();
		segExample.simpleMultipleFingerExample();
		
		FingerEnrollmentExample fingerExample = new FingerEnrollmentExample();
		fingerExample.runTestExamples();
		SmartContainerTest containerTest = new SmartContainerTest();
		containerTest.runTestExamples();
		*/		
	}
}
