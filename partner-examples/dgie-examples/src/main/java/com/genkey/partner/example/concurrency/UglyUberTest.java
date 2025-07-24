package com.genkey.partner.example.concurrency;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.FormatUtils;

/**
 * 
 * Stress Test that operates on a set of subjects using optionally preprocessed Enrollments.
 * 
 * This is actually performing exhaustive concurrent functional tests in a multi-threaded loop
 * for all supported operations at the same time.
 * 
 * @author Gavan
 *
 */

/**
 * Actually it is a beautiful test, but it is not using ParallelComputer from JUNIT4 as planned.
 * <p>
 * This is running multiple concurrency tests at the same time for the full set of functional stress
 * tests.
 * <table>
 * 	<tr>
 * 		<th>Test</th>
 * 		<th>Description></th>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyResetTest</td>
 * 		<td>Resets test data</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyEnrolTest</td>
 * 		<td>Performs biometric enrolments</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyQueryTest</td>
 * 		<td>Performs 1:N identification queries</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyVerifyTest</td>
 * 		<td>Performs 1:1 authentication checks</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyFetchTest</td>
 * 		<td>Performs image access requests</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyBiographicInsertTest></td>
 * 		<td>Performs biographic inserts</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyBiographicFetchTest</td>
 * 		<td>Performs biographic fetch tests</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ConcurrencyLegacyVerifyTest</td>
 * 		<td>Performs legacy verifications</td>
 * 	</tr>
 * </table>
 * @author Gavan
 *
 */
public class UglyUberTest extends ParallelTestRunner{

	
	private static final String ConcurrencyResetTestName = "ConcurrencyResetTest";
	
	//static JCommander commander;
	
	
	
	public static void main(String [] args) {
		UglyUberTest test = new UglyUberTest();
//		commander = new JCommander(test,args);
		test.processCommandLine(args);		
		
	}
	
	public void mainTest() {
		UglyUberTest test = new UglyUberTest();
		test.execute();		
	}
	
	
	public void processCommandLine(String [] args) {
		if (args.length == 0) {
			mainTest();
		} else {
			String testName=args[0];
			ConcurrencyTest test = getChildTest(testName);
			if (test != null) {
				runConcurrencyTest(test);
			} else {
				ExampleModule.runTestAsCommand(this, testName);
			}
		}
	}
	

	public void resetTestData() {
		ConcurrencyResetTest test = new ConcurrencyResetTest();
		runConcurrencyTest(test);
	}

	public void runConcurrencyTest(ConcurrencyTest test) {
		//String 
		String name = test.getTestName();
		FormatUtils.printBanner("Executing concurrency test " + name);
		test.runStressTest();
		FormatUtils.printBanner("Test " + name + " complete");
	}

	
	private void init() {
		
		this.addTestCase(new ConcurrencyBiographicFetchTest());
		this.addTestCase(new ConcurrencyBiographicInsertTest());
		this.addTestCase(new ConcurrencyEnrolTest());
		
		this.addTestCase(new ConcurrencyQueryTest());
		this.addTestCase(new ConcurrencyVerifyTest());
		
		this.addTestCase(new ConcurrentLegacyVerifyTest());
		this.addTestCase(new ConcurrencyFetchTest());
	}
	
	public UglyUberTest() {
		init();
	}
	
	private ConcurrencyTest getChildTest(String testName) {
		ConcurrencyTest result = null;
		for(ConcurrencyTest test : super.getTestCases()) {
			if ( testName.equalsIgnoreCase(test.getTestName())) {
				result=test;
				break;
			}
		}
		return result;
	}
	
	private List<String> getTestNames() {
		List<String> testClasses = new ArrayList<>();
		testClasses.add("mainTest");
		testClasses.add("showTests");
		testClasses.add("resetTestData");
		testClasses.add("replicationTestGenerator");
		for(ConcurrencyTest test : super.getTestCases()) {
			testClasses.add(test.getTestName());
		}
		return testClasses;
	}
 	
	public void showTests() {
		FormatUtils.printObject("TestCases", CollectionUtils.containerToString(getTestNames(),"\n"));
	}

	public void replicationTestGenerator() {
		FormatUtils.println("Entering replication test generator");
		ReplicationTestGenerator test = new ReplicationTestGenerator();
		runConcurrencyTest(test);
	}
	
}
