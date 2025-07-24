package com.genkey.abisclient.examples.utils;

import java.util.Map;

import com.genkey.abisclient.SystemSettings;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.verification.VerificationEnums.BioHASHGenerationMode;

public class ExampleSettings {
	
	 public static int DevelopmentDomain = 1;

     // Note the following property values are arbitrary, and any application can use any convention it wants
     // but should avoid use of names that start with the prefix abisClient 
     public static String PROP_USE_LEGACY = "testLibrary.useLegacy";
     public static String PROP_ENABLE_BC = "testLibrary.enableBackwardsCompatibility";
     public static String PROP_FULL_TESTS = "testLibrary.runFullTests";
     public static String PROP_BENCHMARK_TESTS = "testLibrary.runBenchmark";
     public static String PROP_LICENSE_TEST = "testLibrary.runLicenseTest";
     public static String PROP_SELECTED_TEST = "testLibrary.runSelectedTest";
     public static String PROP_STARTUP_TEST = "testLibrary.runStartUpTest";
     public static String PROP_SECURITY_DOMAIN = "testLibrary.securityDomain";
     public static String PROP_SERVER_DOMAIN = "testLibrary.serverEmulationDomain";
     public static String PROP_BIOHASH_MODE="testLibrary.biohashMode";
     public static String PROP_LOG_CONFIG = "testLibrary.logConfiguration";
     public static String PROP_SAVE_INI = "testLibrary.saveSystemINI";

     public static String PROP_QTHRESHOLD = "testLibrary.qualityThreshold";
     public static String PROP_SAMPLE_COUNT = "testLibrary.targetSampleCount";

     private static final String TestIniFile = "test-abis-client.ini";//     
     public static String DefaultTestPropertyFile = "test-abis-client.ini";
     public static String DefaultSystemPropertyFile = "abisClient.ini";
     public static int DefaultQualityThreshold = 13;
     public static int DefaultTargetSampleCount = 2;


     public static String BenchMarkImage = "benchMark";	
     
     static ExampleSettings instance = new ExampleSettings();
     
     private ExampleSettings() {
    	 
     }
     
     
     public static ExampleSettings getInstance() {
    	 return instance;
     }
     
     public void initialize() {
    	 
    	 try {
    		 //String iniFile = SystemSettings.getPropertyValue(InternalSettings.ENV_INI_FILE, defaultValue)
	    	 SystemSettings.setSystemSetting(InternalSettings.ENV_INI_FILE, DefaultSystemPropertyFile);
	    	 
	    	 SystemSettings.loadSystemDefaults();
	    	 
	    	 // Load the test Settings
	         SystemSettings.loadFromFile(DefaultTestPropertyFile);
	
	         if (this.isSaveSystemINIFile())
	         {
	             SystemSettings.saveToFile(DefaultSystemPropertyFile);
	         }    	 
    	 } catch (Exception e) {
    		 ExampleModule.handleException(e, true);
    	 }
    		 
     }
     
     
     public void runSystemSettingExamples()
     {
         // Check System Defaults are loaded and present
         SystemSettings.loadSystemDefaults();

         // Access the current value of System properties
         Map<String, String> properties = SystemSettings.getAllProperties();

       
         // Examine the internal system settings that are loaded by call to LoadSystemDefaults
         InternalSettings internalConfig = InternalSettings.getInstance();

         // See what the preconfigured values are for legacy version and latest version and 
         // configuration

         // Modify the security kernel to the development value - which is not how this is normally done
         SystemSettings.setSystemSetting(InternalSettings.ENV_WDSK_CODE, 1);

         //Modify the default location for the System Logger configuration file
         String systemLogFile = internalConfig.getLogFileConfiguration();

         // Query for test settings with the default
         String logConfiguration = this.getLogConfiguration(systemLogFile);
  
         internalConfig.setLogFileConfiguration(logConfiguration);

         systemLogFile = internalConfig.getLogFileConfiguration();

     }     
     
     public boolean isSaveSystemINIFile() {
		return SystemSettings.getPropertyBoolean(PROP_SAVE_INI, false);
     }
     
     
     public boolean isEnableBackwardsCompatibility() {
    	 return SystemSettings.getPropertyBoolean(PROP_ENABLE_BC, false);
     }

     public void setEnableBackwardsCompatibility(boolean value) {
    	 SystemSettings.setSystemSetting(PROP_ENABLE_BC, value);
     }
     

     public boolean isUseLegacy() {
    	 return SystemSettings.getPropertyBoolean(PROP_USE_LEGACY, false);
     }

     public void setUseLegacy(boolean value) {
    	 SystemSettings.setSystemSetting(PROP_USE_LEGACY, value);
     }
     
     public int getQualityThreshold() {
    	 return SystemSettings.getPropertyInt(PROP_QTHRESHOLD, -1);
     }
     
     public void setQualityThreshold(int value) {
    	 SystemSettings.setSystemSetting(PROP_QTHRESHOLD, value);
     }
     
     public int getTargetSampleCount() {
    	 return SystemSettings.getPropertyInt(PROP_SAMPLE_COUNT, -1);
     }
     
     public void setTargetSampleCount(int value) {
    	 SystemSettings.setSystemSetting(PROP_SAMPLE_COUNT, value);
     }
     
     public int getSecurityDomain() {
     	return SystemSettings.getPropertyInt(PROP_SECURITY_DOMAIN, -1);
     }
     
     public void setSecurityDomain(int value) {
     	SystemSettings.setSystemSetting(PROP_SECURITY_DOMAIN, value);
     }
     
     public int getServerEmulationDomain() {
     	return SystemSettings.getPropertyInt(PROP_SERVER_DOMAIN, -1);
     }
     
     public void setServerEmulationDomain(int value) {
     	SystemSettings.setSystemSetting(PROP_SERVER_DOMAIN, value);
     }
     
     public BioHASHGenerationMode getGenerationMode() {
    	 int biohashMode = SystemSettings.getPropertyInt(PROP_BIOHASH_MODE, 1);
    	 return BioHASHGenerationMode.fromOrdinal(biohashMode);
     }
     
     public boolean isStartupTest() {
    	 return SystemSettings.getPropertyBoolean(PROP_STARTUP_TEST, false);
     }
 
     public boolean isLicenseTest() {
    	 return SystemSettings.getPropertyBoolean(PROP_LICENSE_TEST, false);
     }
 
     public boolean isSelectedTest() {
    	 return SystemSettings.getPropertyBoolean(PROP_SELECTED_TEST, false);
     }
      
     public boolean isRunFullTests() {
    	 return SystemSettings.getPropertyBoolean(PROP_FULL_TESTS, true);
     }
     
     public boolean isRunBenchmark() {
    	 return SystemSettings.getPropertyBoolean(PROP_BENCHMARK_TESTS, false);
     }
     
     public String getLogConfiguration() {
    	 return getLogConfiguration("");
     }
     
     public String getLogConfiguration(String defaultValue) {
     	return SystemSettings.getPropertyValue(PROP_LOG_CONFIG, defaultValue);
     }
       
     

	public String toString()
     {
         StringStream stream = new StringStream();
         stream.printAttribute(PROP_FULL_TESTS, isRunFullTests());
         stream.printAttribute(PROP_BENCHMARK_TESTS, isRunBenchmark());
         stream.printAttribute(PROP_LOG_CONFIG, getLogConfiguration());
         stream.printAttribute(PROP_USE_LEGACY, isUseLegacy());
         stream.printAttribute(PROP_ENABLE_BC, isEnableBackwardsCompatibility());
         stream.printAttribute(PROP_SECURITY_DOMAIN, getSecurityDomain());
         stream.printAttribute(PROP_SERVER_DOMAIN, getServerEmulationDomain());
         return stream.toString();
     }


	public String getTestImage(String settingName) {

        String defaultImage = settingName + "." + TestDataManager.getImageFormat();
        String setting = "testImage." + settingName;
        return SystemSettings.getPropertyValue(setting, defaultImage);	
   }
 

}
