package com.genkey.partner.example;

import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISRestClient;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.abisclient.service.RestClientException;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.components.GenericRestClient;
import com.genkey.partner.biographic.BiographicIdentifier;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.example.concurrency.ConcurrencyTest;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.GKTimerTask;
import com.genkey.platform.utils.Logging;

public class DRFailoverTest  extends ConcurrencyTest{

	
	boolean skipSetup=false;

	static int startSubject=1000;
	static int nSubjects=10;
		
	
	boolean cancel=false;
	
	public static void main(String [] args) {
		DRFailoverTest test = new DRFailoverTest(false);
		test.runTestExamples();
	}
	
	
	public DRFailoverTest() {
		this(false);
	}
	
	
	public DRFailoverTest(boolean skipSetup) {
		this.skipSetup=skipSetup;
	}


	@Override
	protected void setUp() {
		GenkeyABISRestClient client = new GenkeyABISRestClient();
		if (!skipSetup) {
			super.setUp();			
		}
		super.setStartSubject(startSubject);
		super.setSubjectCount(nSubjects);
		
		// Performs stand-alone check on startup
		RestServices.getInstance().checkFailover(true);
		
		GenkeyABISService service =  ABISServiceModule.getABISService();
		service.checkFailover();
		
		
		checkBiographicSubjectsExist();		
		GenericRestClient.setFailoverTimeout(3000);
	}

	


	public boolean isCancel() {
		return cancel;
	}


	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}


	protected void runAllExamples() {
		testRestart();
		testOnTheFly();
	}	
	
	
	// test failover on restart
	public  void testRestart() {
		PartnerTestSuite.init();
	}
	
	// test failover on the fly
	public void testOnTheFly() {
		BiographicService service = BiographicServiceModule.getBiographicService();
		
		boolean status = service.checkFailover();
		
		if (!status) {
			super.printError("Existing because no servers available");
			return;
		}
		
		
		boolean available = service.testAvailable();
		assertTrue(available);
		int count=0;
		String domainName = this.getDomainName();
		while (! isCancel()) {
			for(int ix=0; ix < this.getSubjectCount() ; ix++ ) {
				long subject = this.getStartSubject() + ix;
	try {
		String externalId = BiographicIdentifier.resolveExternalID(String.valueOf(subject), domainName);
		boolean status2 = service.existsBiographicRecord(externalId);
		if (! service.isSuccess()) {
			// Check explicitly for failover
			service.checkFailover(false, 0);
		} else {
			printMessage("Service running well!");
		}
	} catch (RestClientException exception) {
		String errorStack = Logging.format(exception);
		int statusCode = exception.getStatusCode();
		if (statusCode == RemoteAccessService.ConnectException) {
			while ( ! service.checkFailover() )
			{
				printError("Waiting for service to become available");
				Commons.waitMillis(5000);
			}
			boolean status3 = service.checkFailover();
			printMessage("Failover pending look finished with availability " + status3);
		} else {
			printError("Failed to access " + exception);
		}
		
	}
			}
		}
	}


	@Override
	public void stressTest() {
		// TODO Auto-generated method stub
		
	}
	
}

class CancelTimeoutTask extends GKTimerTask {

	@Override
	protected void runTask() {
		// TODO Auto-generated method stub
		
	}
	
}
