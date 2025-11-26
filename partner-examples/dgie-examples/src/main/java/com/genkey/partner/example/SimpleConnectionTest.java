package com.genkey.partner.example;

import java.util.Date;

import org.junit.Test;

import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.platform.utils.FormatUtils;

/**
 * Code example to show how to initialize the SDK and to test the connection.
 * <p>
 * Initialization is performed through the DGIEServiceModule and ABISServiceModule facades which
 * are initialized with host-name, port and the default domain name that should be applied for 
 * system level scoping of biographic-identifier names
 * <p>
 * The examples show how to test for availability and how to interrogate parameter from the service
 * 
 * @author Gavan
 *
 */
public class SimpleConnectionTest extends PartnerExample {

	
	public static boolean runInline;
	

	public static void main(String [] args) {
		Date date = new Date(1597805718000l);
		FormatUtils.printBanner("Date is " + date);
		
		FormatUtils.printBanner("Modified test");
		SimpleConnectionTest test = new SimpleConnectionTest();
		//test.runTestCase4();
		//test.runTestExamples();
		test.processCommandLine(args);
	}
	
	
	@Override
	protected void runAllExamples() {
//		testABISReset();
//		testInitialization();
		testStandardConnection();
		FormatUtils.printBanner("Tests complete");
	}


	@Override
	protected void setUp() {
		// Block the default initialization routine
		ExampleTestUtils.loadDefaultSettings();
	}

	/**
	 * This performs an initialization of services and then performs a remote restart on the
	 * ABIS services that run on the server.
	 * <p>
	 * Note this is a test action only and will not work unless the server is configured to
	 * run in test mode.
	 */
	@Test
	public void testABISReset() {
		DGIEServiceModule.initCoreServices(getPrimaryHost(), getServicePort(),PartnerExample.getPartnerDomainName());
		
		// To access test services then cast the 
		TestABISService abisService = (TestABISService) ABISServiceModule.getABISService();
		abisService.systemReset();
	}

	
	@Test
	public void testInitialization() {
		super.setUp();
	}
	
	/**
	 * Shows the standard pattern for initializing the SDK and accessing REST services
	 */
	@Test
	public void testStandardConnection() {
		
		String ServiceHost = PartnerExample.getPrimaryHost();
		int ServicePort = PartnerExample.getServicePort();
		String DomainName = PartnerExample.getPartnerDomainName();
		String LegacyServiceHost = PartnerExample.getLegacyHost();
		String DrServer = PartnerExample.getSecondaryHost();
		
		
		// Initialize core services 

		printResult("ServiceHost", ServiceHost);
		printResult("LegacyServiceHost", LegacyServiceHost);
		printResult("DrServer", DrServer);
		printResult("ABIS PORT", ServicePort);
		printResult("DomainName", DomainName);
		
		
		
		RestServices.getInstance().setABISPort(ServicePort);

		DGIEServiceModule.initCoreServices(ServiceHost, ServicePort,DomainName);
				
		// Initialize the legacy service
		printHeader("Initializing Legacy service");
		DGIEServiceModule.initLegacyService(LegacyServiceHost, ServicePort);

		//if (PartnerExample.UseDR) {
		println("Initializing failover server");
		DGIEServiceModule.setFailoverServer(DrServer);
		
		// Performs stand-alone check on startup
		println("Check for failover");

		FormatUtils.printBanner("Full failover tests");
		RestServices.getInstance().checkFailover(true);
		
		
		RestServices serviceManager = RestServices.getInstance();
		
		//}
		// Generic method to access a service from RestServices
		LegacyMatchingService service = RestServices.getInstance().accessServiceInstance(LegacyMatchingService.class);
		

		// Short cut methods for accessing extended services
		// Access to core ABIS service
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();				

		
		println("Testing availability of services");
		// Check the connection to ABIS Service
		if (!abisService.testAvailable()) {
			// Returns the HTTP status code from last call
			int status = abisService.getStatusCode();
			
			String errorMessage = abisService.getLastErrorMessage();
			
			PrintMessage("Failed access to ABIS service with HTTP status code " + status + " error " + errorMessage );
		} else {
			// If connection is successful the getLastErrorMessage returns the connection status
			String connectionMessage = abisService.getLastErrorMessage();
			FormatUtils.printBanner(connectionMessage);
			
			// The following makes a separate test for access to the backend ABIS system
			String abisConnection = abisService.testABISConnection();
			FormatUtils.printObject("ABIS Core Connection",abisConnection);
		}
		
		// Repeat connection tests for the other servoces
		checkRemoteServiceConnection(biographicService);
		checkRemoteServiceConnection(legacyService);
				
	}

	@Test
	public void testABISService() {
		ABISServiceModule.init(getPrimaryHost(), getServicePort(), getPartnerDomainName());
		// Access to core ABIS service
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		checkRemoteServiceConnection(abisService);
	}

	@Test
	public void testRemoteABISConnection() {
		ABISServiceModule.init(getPrimaryHost(), getServicePort(), getPartnerDomainName());
		GenkeyABISService abisService = ABISServiceModule.getABISService();
		String connection = abisService.testABISConnection();
		if (connection != null) {
			printObject("ABIS Connection String", connection);
		} else {
			printError("Remote ABIS connecton not available");
		}
	}
	
	@Test
	public void testBiographicService() {
		BiographicServiceModule.initService(getPrimaryHost(), getServicePort(), getPartnerDomainName());
		BiographicService biographicService = DGIEServiceModule.getBiographicService();
		checkRemoteServiceConnection(biographicService);
	}

	@Test
	public void testLegacyMatchingService() {
		DGIEServiceModule.initLegacyService(getLegacyHost(), getServicePort());
		LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
		LegacyMatchingService legacyService2 = RestServices.getInstance().accessServiceInstance(LegacyMatchingService.class);
		
		checkRemoteServiceConnection(legacyService);
	}
	
	
	/**
	 * Tests for access and interrogates properties of a RemoteAccessService
	 * @param service   RemoteAccessService to be tested
	 */
	private static void checkRemoteServiceConnection(RemoteAccessService service) {
		PrintMessage("Testing service "+ service.getClass());
		if (! service.testAvailable()) {
			String hostName = service.getHostName();
			int port = service.getPort();
			int statusCode = service.getStatusCode();
			String errorMessage = service.getLastErrorMessage();
			PrintMessage("Error connecting to service @" + hostName +":" + port + " with Status="  + statusCode + " :" + errorMessage);
		} else {
			// if all is okay then simply bring the message
			PrintMessage("Connection test successful");
			FormatUtils.printBanner(service.getLastErrorMessage());
		}
	}

	
	
}
