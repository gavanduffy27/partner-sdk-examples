package com.genkey.partner.example.concurrency;

import org.junit.Test;

import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.partner.utils.concurrency.ObjectWrapperTask;
import com.genkey.partner.utils.concurrency.SubjectListController;
import com.genkey.partner.utils.concurrency.TestTask;

/**
 * Bucket suite for ad-hoc testing of the concurrency test infrastructure components
 * @author Gavan
 *
 */
public class ConcurrencyAdhocTests extends ConcurrencyTest{
	
	
	@Override
	protected void runAllExamples() {
		checkSubjectEnroll();
		runTestTaskController();
	}

	
	
	
	@Override
	public void stressTest() {
		
	}




	/**
	 * Test was put in place to debug structural issues on the cached Subject references which
	 * are exploited to enable very high stress loads on ABIS from a single client.
	 */
	@Test
	public void checkSubjectEnroll() {
		SubjectEnrollmentReference enrolRef = EnrollmentUtils.accessEnrollmentRecord(1000, 1, "test");
		int[] fingers = enrolRef.getFingersPresent();
		for(int finger : fingers) {
			for (FingerEnrollmentReference fingerRef : enrolRef.getReferencesForFinger(finger)) {
				int finger2 = fingerRef.getFingerID();
				ReferenceDataItem refData = fingerRef.getReferenceDataItem();
				int finger3 = refData.getFingerID();
				ImageData image = fingerRef.getImageData();
			}
		}
	}


	/**
	 * Tests the subject task scheduler
	 */
	@Test 
	public void runTestTaskController() {
		SubjectListController controller = new SubjectListController();
		controller.setSubjectList(10, 20, 5);
		controller.setIterationCount(3);
		while (controller.hasMoreTasks()) {
			@SuppressWarnings("unchecked")
			Long subject = controller.getNextTaskObject(Long.class);
			if (subject != null) {
				println("Subject " + subject);
			} else {
				println("No more tasks");
			}
		}
	}
	
	

}
