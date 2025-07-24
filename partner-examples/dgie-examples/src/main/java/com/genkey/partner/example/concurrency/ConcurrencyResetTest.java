package com.genkey.partner.example.concurrency;

import com.genkey.abisclient.service.TestABISService;

public class ConcurrencyResetTest extends ConcurrencyTest{

	
	public static void main(String [] args) {
		ConcurrencyResetTest test = new ConcurrencyResetTest();
		test.runTestCase4();
	}		
	
	@Override
	protected void setUp() {
		super.setUp();
		clearAllTestData();
	}
	
	@Override
	public void stressTest() {
//		super.commitABISDeletes();
		TestABISService service = getTestABISService();
		service.commitSubjectDeletes();				
	}

	public void clearAllTestData() {
		for(int ix=0; ix < 10; ix++) {
			resetSubjects(ix);
			super.deleteTestSubjectDomain();
			/*
			super.checkSubjectsPresent(false);
			super.deleteBiographicSubjects();
			*/
		}		
	}
	
	private void resetSubjects(int cycleShift) {
		super.setSubjectCount(DefaultMaxSubjectCount);
		super.setCyclicShift(cycleShift);		
	}
	
}
