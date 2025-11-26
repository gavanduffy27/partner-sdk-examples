package com.genkey.partner.example;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.examples.ExampleSuite;
import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.RestServices;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.platform.utils.FileUtils;

public class PartnerTestSuite extends ExampleSuite{
	
	public static boolean flgInit=false;
	
	public static boolean flgClientInit=false;
	
	// Define a settings file
	//static String SETTINGS_FILE="./test/remoteTest.ini";
	static String SETTINGS_FILE="./test/localTest.ini";
	
	
	
	//static PropertyMap prop
	
	public static void init() {
		if (!flgInit) {
			try {
				// Comment out this line to use other mechanisms
				ExampleTestUtils.setSettingsFile(SETTINGS_FILE);
				
				// Generic method for establishing settings
				ExampleTestUtils.loadDefaultSettings();

				String ServiceHost = PartnerExample.getPrimaryHost();
				int ServicePort = PartnerExample.getServicePort();
				String DomainName = PartnerExample.getPartnerDomainName();
				String LegacyServiceHost = PartnerExample.getLegacyHost();
				String DrServer = PartnerExample.getSecondaryHost();
				
				//ABISClientLibrary.initializeWithSettings(SettingsFile);
				RestServices.getInstance().setABISPort(0);
				ABISClientLibrary.initializeDefault();
				DGIEServiceModule.initCoreServices(ServiceHost, ServicePort, DomainName);
				DGIEServiceModule.initLegacyService(LegacyServiceHost, ServicePort);
				DGIEServiceModule.setFailoverServer(DrServer);
				// Performs stand-alone check on startup
				RestServices.getInstance().checkFailover(true);
				
				String cfgPath = ImageContextSDK.getConfigurationPath();
				FileUtils.setConfigurationPath(cfgPath);
			} catch (Exception e) {
				handleException(e);
			}
			flgInit=true;
		}
	}	
	
	
	public static void initClient() {
		
		if (! flgClientInit) {
			try {
				ABISClientLibrary.initializeDefault();			
				String cfgPath = ImageContextSDK.getConfigurationPath();
				FileUtils.setConfigurationPath(cfgPath);			
				
			} catch (Exception e) {
				handleException(e);
			}
		}
	}
	
	@Override
	protected void initExampleSuite() {
		init();
	}
	
	public static GenkeyABISService getABISService() {
		return ABISServiceModule.getABISService();
	}
	
	public static BiographicService getBiographicService() {
		return BiographicServiceModule.getBiographicService();		
	}
	
	public static LegacyMatchingService getLegacyService() {
		return DGIEServiceModule.getLegacyService();		
	}
	

}
