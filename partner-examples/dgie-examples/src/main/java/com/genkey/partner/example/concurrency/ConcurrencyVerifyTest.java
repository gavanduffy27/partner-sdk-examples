package com.genkey.partner.example.concurrency;

import org.junit.Test;

/**
 * Concurrent Biometric Verification Tests
 * @author Gavan
 *
 */
public class ConcurrencyVerifyTest  extends ConcurrencyTest{
	private static final int Concurrency = 10;
	private static final int SubjectCount =10;
	private static final int IterationCount=50;
	private static final int CyclicShift=5;
	private static final int StartSubject = 1030;

	public static void main(String [] args) {
		ConcurrencyVerifyTest test = new ConcurrencyVerifyTest();
		test.runTestCase4();
	}	
	
	@Override
	protected void setUp() {
		super.setUp();
		super.setStartSubject(StartSubject);
		super.setConcurrency(Concurrency);
		super.setSubjectCount(SubjectCount);
		super.setIterationCount(IterationCount);
		super.setCyclicShift(CyclicShift);
		super.checkSubjectsExist();
	}	


	@Override
	protected void tearDown() {
		super.tearDown();
		super.deleteCreatedSubjects();
	}	

	@Override
	protected void runAllExamples() {
		stressTest();
	}

	@Test
	public void stressTest() {
		VerifyTestThread taskThread = new VerifyTestThread();
		super.executeThreadTest(taskThread);		
	}

}
