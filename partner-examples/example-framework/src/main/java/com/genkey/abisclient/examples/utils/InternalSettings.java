package com.genkey.abisclient.examples.utils;

import com.genkey.abisclient.SystemSettings;

public class InternalSettings {

    static public String ENV_HARDWARE_ID = "abisClient.hardwareId";
    static public String ENV_WDSK_CODE = "abisClient.wdskCode";
    static public String ENV_BSK_CODE = "abisClient.bskCode";
    static public String ENV_USE_LEGACY = "abisClient.useLegacy";
    static public String ENV_USE_EMBEDDED = "abisClient.embedded";
    static public String ENV_CONFIG_DIRECTORY = "abisClient.configDirectory";
    static public String ENV_LEGACY_VERSION = "abisClient.legacyVersion";
    static public String ENV_LATEST_VERSION = "abisClient.latestVersion";
    static public String ENV_LOG_CONFIGURATION = "abisClient.logConfig";
    static public String ENV_INI_FILE = "abisClient.defaultPropertyFile";
    static public String DefaultPropertyFile = "abisClient.ini";
    
    static InternalSettings instance = new InternalSettings();
    
    private InternalSettings() {
    	
    }
    
    public static InternalSettings getInstance() {
    	return instance;
    }
    
    public int getLegacyVersion() {
    	return SystemSettings.getPropertyInt(ENV_LEGACY_VERSION, -1);
    }
    
    public void setLegacyVersion(int value) {
    	SystemSettings.setSystemSetting(ENV_LEGACY_VERSION, value);
    }
    
    public int getLatestVersion() {
    	return SystemSettings.getPropertyInt(ENV_LATEST_VERSION, -1);
    }
    
    public void setLatestVersion(int value) {
    	SystemSettings.setSystemSetting(ENV_LATEST_VERSION, value);
    }
    
    public String getConfigurationDirectory() {
    	return SystemSettings.getPropertyValue(ENV_CONFIG_DIRECTORY, "");
    }
    
    public void setConfigurationDirectory(String value) {
    	SystemSettings.setSystemSetting(ENV_CONFIG_DIRECTORY, value);
    }

    public String getLogFileConfiguration() {
    	return SystemSettings.getPropertyValue(ENV_LOG_CONFIGURATION, "");
    }
    
    public void setLogFileConfiguration(String value) {
    	SystemSettings.setSystemSetting(ENV_LOG_CONFIGURATION, value);
    }
 
    public int getSecurityDomain() {
    	return SystemSettings.getPropertyInt(ENV_WDSK_CODE, -1);
    }
    
    public void setSecurityDomain(int value) {
    	SystemSettings.setSystemSetting(ENV_WDSK_CODE, value);
    }
    
    public int getServerEmulationDomain() {
    	return SystemSettings.getPropertyInt(ENV_BSK_CODE, -1);
    }
    
    public void setServerEmulationDomain(int value) {
    	SystemSettings.setSystemSetting(ENV_BSK_CODE, value);
    }
    
    public boolean isEmbedded() {
    	return SystemSettings.getPropertyBoolean(ENV_USE_EMBEDDED, false);
    }

    public void setEmbedded(boolean value) {
    	SystemSettings.setSystemSetting(ENV_USE_EMBEDDED, value);
    }

    public boolean isUseLegacy() {
    	return SystemSettings.getPropertyBoolean(ENV_USE_LEGACY, false);
    }

    public void setUseLegacy(boolean value) {
    	SystemSettings.setSystemSetting(ENV_USE_LEGACY, value);
    }
 
    public String ToString()
    {
        StringStream stream = new StringStream();

        stream.printAttribute(ENV_CONFIG_DIRECTORY, this.getConfigurationDirectory());
        stream.printAttribute(ENV_LOG_CONFIGURATION, this.getLogFileConfiguration());
        stream.printAttribute(ENV_LEGACY_VERSION, this.getLegacyVersion());
        stream.printAttribute(ENV_LATEST_VERSION, this.getLatestVersion());
        stream.printAttribute(ENV_USE_LEGACY, this.isUseLegacy());
        stream.printAttribute(ENV_USE_EMBEDDED, this.isEmbedded());
        stream.printAttribute(ENV_WDSK_CODE, this.getSecurityDomain());
        stream.printAttribute(ENV_BSK_CODE, this.getServerEmulationDomain());

        return stream.toString();
    } 
    

}
