package com.genkey.partner.example.concurrency;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.test.framework.GKTestCase4;
import com.genkey.platform.utils.FormatUtils;

/**
 * Mechanism to run mixed concurrency tests at the same time.  Note this is not the same mechanism used
 * to enable the concurrency tests. It is more of a substitute for the JUNIT ParallelComputer class
 * that allows for different concurrency tests to be run at the same time.
 * <p>
 * Initialized by a set of ConcurrencyTest which are then separately run each in their own
 * thread.  Note each test will then spawn its own threads independently with its own
 * task-list.
 * 
 * @author Gavan
 *
 */
public class ParallelTestRunner {

	static org.slf4j.Logger logger = LoggerFactory.getLogger(ParallelTestRunner.class);
	
	List<ConcurrencyTest> testCases = new ArrayList<>();
	
	public void addTestCase(ConcurrencyTest testCase) {
		this.testCases.add(testCase);
	}
	
	public void execute() {
		List<Thread> threadList = new ArrayList<>();
		
		
		// This enforces ABIS reset actions until the end of all testing
		boolean enforcePendingDelete = PartnerExample.isEnforcePendingDelete();
		
		/**
		 * This flag when set suppresses the ABISSystemReset call that would otherwise
		 * be called multiple times during teardown phase
		 */
		PartnerExample.setEnforcePendingDelete(true);
		
		/**
		 * Run setup for all tests first before executing anythreads
		 */
		for(ConcurrencyTest testCase : this.testCases) {
			FormatUtils.println("Running setUp on " + testCase.getTestName());
			testCase.runSetUp4();
		}
		
		for(ConcurrencyTest testCase : this.testCases) {
			TestCaseThread caseThread = new TestCaseThread(testCase); 
			threadList.add(new Thread(caseThread));
		}
		
		for(Thread thread : threadList) {
			thread.start();
		}

		for(Thread thread : threadList) {
			try {
				thread.join();
			}catch (Exception e) {
				logger.error("Failed tto oin thread", e);
			}
		}

		for(ConcurrencyTest testCase : this.testCases) {
			FormatUtils.println("Running tearDown on " + testCase.getTestName());
			testCase.runTearDown4();
		}
		
		/**
		 * Commit the pending deletes to the core ABIS system which will include a system restart
		 */
		PartnerExample.commitABISDeletes();
		PartnerExample.setEnforcePendingDelete(enforcePendingDelete);
		
		FormatUtils.printBanner("Concurrent test complete");
	}

	public List<ConcurrencyTest> getTestCases() {
		return testCases;
	}
	
	
	
}

class TestCaseThread implements Runnable {

	ConcurrencyTest testCase;
	
	public TestCaseThread(ConcurrencyTest testCase) {
		this.testCase=testCase;
	}
	
	@Override
	public void run() {
		testCase.stressTest();
	}
	
}
