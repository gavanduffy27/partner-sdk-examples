package com.genkey.partner.workshop;

import java.io.File;

import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;

/**
 * Performs initilization scenarios.
 * 
 * 		-Dabis.partner.settings_file=partnerExamples.ini
 * 
 * 	OR
 * 
 * 		SET ABIS_PARTNER_SETTINGS_FILE=partnerExamples.ini
 * 
 *  Default is to use partnerExamples.ini from current folder for this test project.
 * 
 */
public class InitializationExample extends BMSWorkshopExample {

	public static void main(String[] args) {
		PartnerExample test = new InitializationExample();
		test.processCommandLine(args);
	}
	
	@Override
	protected void setUp() {
		File file = new File(".");
		try {
			String currentDirectory = file.getCanonicalPath();
			printResult("Current path",currentDirectory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// to block default test setup
		//super.setUp();
	}

	protected void runAllExamples() {
		testConnectFromProperties();
	}

	public void testConnectFromProperties() {
		ExampleTestUtils.setCodeDefaultSettings();
		ExampleTestUtils.loadDefaultSettings();
		
		String hostName = PartnerExample.getPrimaryHost();
		int port = PartnerExample.getServicePort();
		String DomainName = PartnerExample.getPartnerDomainName();

		// Initialise core services ..
		// Currently for production this would be //10.22.74.51, 8091, BMS
		DGIEServiceModule.initCoreServices(hostName, port, DomainName);
		
		// To use Legacy services .. not required
		DGIEServiceModule.initLegacyService(hostName, port);

		// Access the service objects
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
		
		// Test for access on main ABIS service
		boolean status = abisService.testAvailable();
		
		printResult("Status", status);
		
		// Return the message on last call of current thread
		String message = abisService.getLastErrorMessage();
		printResult("Status message", message);
		
		
		// Run the test for ABIS connection
		String abisConnection = abisService.testABISConnection();		
		this.printHeaderResult("ABIS Connection", abisConnection);
		
		// Check the biographic service
		status = biographicService.testAvailable();
		printResult("Biographic Status", biographicService.getLastErrorMessage());
		
	}
	
}
