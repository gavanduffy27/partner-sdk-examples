package com.genkey.partner.example.concurrency;

import com.genkey.abisclient.service.GenkeyABISRestClient;
import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;

/**
 * 4 - 
 * 5 - max
 * 6 - max
 * 7 - 0-24
 * 8 - max
 * 9 - max
 * @author gavan0
 *
 */
public class ReplicationTestGenerator extends ConcurrencyTest{
	
	static int TestConcurrency=1;
	static int startSubject=1040;
	static int nSubjects=20;
	
	private static final int CyclicShift=4;	
	
	public static boolean flgCreate=false;
	
	
	boolean flgDelete;
	
	public ReplicationTestGenerator() {
		this(false);
	}
	
	public ReplicationTestGenerator(boolean flgDelete) {
		this.flgDelete=flgDelete;
	}
	
	public static void main(String [] args) {
		ReplicationTestGenerator test = new ReplicationTestGenerator();
		test.setUp();
//		test.testFlushSync();
//		test.createAllData();
//		test.dropAllData();
	}	
	
	@Override
	protected void setUp() {
		super.setUp();
		super.setConcurrency(TestConcurrency);
		super.setStartSubject(startSubject);
		super.setSubjectCount(nSubjects);
		super.setCyclicShift(CyclicShift);
	}	
	
	
	public void testFlushSync() {
		GenkeyABISRestClient restService = (GenkeyABISRestClient) super.getABISService();
		int counter=0;
		while(true) {
			FormatUtils.println("Flushing sync [" + counter++ + "]");
			if (restService.testAvailable()) {
				restService.flushSync();					
			}
			Commons.waitMillis(10000);
		}
	}
	
	public void createAllData() {
		super.checkSubjectsExist();
		
		checkBiographicSubjectsExist();
		checkLegacySubjectsExist();
						
	}

	public void dropAllData() {
//		super.checkSubjectsNotPresent();
		super.checkBiographicSubjectsDeleted();
		super.deleteCreatedLegacySubjects();
		super.deleteBiographicSubjects();
	}
	
	@Override
	protected void runAllExamples() {
		stressTest();
	}	
	
	@Override
	public void stressTest() {
		if (this.flgDelete) {
			dropAllData();
		} else {
			createAllData();			
		}

	}

}
