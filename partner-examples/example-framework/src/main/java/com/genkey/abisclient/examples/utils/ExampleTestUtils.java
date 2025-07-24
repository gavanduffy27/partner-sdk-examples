package com.genkey.abisclient.examples.utils;

import java.io.FileInputStream;

import org.slf4j.LoggerFactory;

import com.genkey.platform.utils.StringUtils;

public class ExampleTestUtils {

	static org.slf4j.Logger logger = LoggerFactory.getLogger(ExampleTestUtils.class);

	
	public static String DefaultSettingsFile="partnerExample.ini";
	public static String OPT_SETTINGS = "abis.partner.settings_file";
	
	
	public static void loadDefaultSettings() {
		loadSettings(OPT_SETTINGS, DefaultSettingsFile);
	}
	
	/**
	 * Loads property settings file
	 * @param settingFile
	 * @param defaultFileName
	 */
	public static void loadSettings(String settingFile, String defaultFileName) {
		String fileName = getPropertyValue(settingFile, defaultFileName);
		if (FileUtils.existsFile(fileName)) {
			try {
				FileInputStream fis = new FileInputStream(fileName);
				System.getProperties().load(fis);
			} catch (Exception e) {
				logger.error("Failed on loading properties from {}", fileName);
			}
		} else {
			if (! fileName.equals(defaultFileName)) {
				logger.info("Specified settings file {} not found", fileName);				
			}
		}
	}
	
	public static String asEnv(String property) {
		String modValue = StringUtils.replaceAnyOfBy(property, ".", '_');
		return modValue.toUpperCase();
	}
	
	/**
	 * Obtains a property name using the following precedence :
	 * <ol>
	 * 	<li> Use system property if defined </li>
	 * 	<li> Use equivalent ENV value formed by substituting _ for . and capitalisation </li>
	 * 	<li> Accept the defaultValue </li>
	 * </ol>
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static String getPropertyValue(String propertyName, String defaultValue) {
		String value = System.getProperty(propertyName);
		if (value == null) {
			value = getPropertyENV(propertyName);
			if (value == null) {
				value=defaultValue;
			}
		}
		return value;
	}

	public static String getPropertyENV(String propertyName) {
		String env = asEnv(propertyName);
		return System.getenv(env);
	}

	public static int getPropertyInteger(String propertyName, int defaultValue) {
		String value = getPropertyValue(propertyName, null);
		int result = defaultValue;
		if (value != null) {
			try {
				result = Integer.parseInt(value);
			} catch (Exception e) {
				logger.error("Failure on parse int for {}", value);
				result=defaultValue;
			}
		}
		return result;
	}
	
}
