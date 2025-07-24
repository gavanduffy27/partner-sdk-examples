package com.genkey.abisclient.examples.utils;

import java.io.FileInputStream;
import java.util.Properties;

import com.genkey.abisclient.examples.BaseTestRunner;
import com.genkey.platform.utils.PropertyMap;

public class TestConfig {
	
	public String iniFile= "./abisClient.ini";
	
	
	PropertyMap properties = null;
	
	private static TestConfig instance = new TestConfig();
	
	
	public static TestConfig getInstance() {
		return instance;
	}

	public PropertyMap getProperties() {
		if (properties == null) {
			properties = new PropertyMap();
			if (FileUtils.existsFile(iniFile)) {
				try {
					FileInputStream fis = new FileInputStream(iniFile);
					properties.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	public void setProperties(PropertyMap properties) {
		this.properties = properties;
	}
	
	public boolean isUseLegacy() {
		String value =  getProperties().getProperty(BaseTestRunner.ARG_USE_LEGACY, "false");
		return Boolean.parseBoolean(value);
	}

}
