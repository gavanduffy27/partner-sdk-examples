package com.genkey.partner.example.concurrency;



//import org.junit.Assert.*;
import org.junit.BeforeClass;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.ext.ImageWrapperSet;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.ImageRequestResponse;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicIdentifier;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.biographic.BiographicServiceModule;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.partner.utils.concurrency.ConcurrencyTestRunner;
import com.genkey.partner.utils.concurrency.SubjectListController;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

import java.util.Set;

import static com.genkey.partner.example.ServiceTestMatcher.*;

/**
 * Implements the core logic for concurrency tests.
 * 
 * @author Gavan
 *
 */
public abstract class ConcurrencyTest extends PartnerExample{
	
	private static long DefaultGracePeriod = 5000;

	static int DefaultConcurrency=4;
	
	int concurrency=DefaultConcurrency;
	
	
	public static long DefaultStartSubject=1000;
	public static int DefaultMaxSubjectCount=100;
	public static int DefaultCycleShift=0;
	
	
	long startSubject=DefaultStartSubject;

	int subjectCount=2;
	
	static int DefaultSampleIndex=1;
	int sampleIndex=DefaultSampleIndex;
	
	static boolean DefaultSimulationMode=false;
	boolean simulationMode=DefaultSimulationMode;

	int iterationCount=1;
	
	String domainName = "EnrollmentSDK";
	
	int cyclicShift=DefaultCycleShift;
	
	int counter=0;
	
	static ConcurrencyTest lockTest=null;
	
	
	private boolean setupOnly=true;
	
	
	public static class ConcurrencyParams {
		public int concurrency=1;
		public int subjectCount=1;
		public int iterationCount=1;
		public int cyclicShift=0;	
	}
	
	@BeforeClass
	public static void initImageDirectory() {
		TestDataManager.setImageRootPath(PartnerImagePath);
		TestDataManager.setImageSet(ImageSet100);		
	}
	
	
	public void commitABISDeletesSync() {
		commitABISDeletesSync(DefaultGracePeriod);
	}
	
	/**
	 * Puts all concurrency tests in a wait phase whilst performing system delete
	 * @param gracePeriod
	 */
	public void commitABISDeletesSync(long gracePeriod) {
		if (PartnerExample.isEnforcePendingDelete()) {
			println("Enforcing pending delete for deferred actions");
			PartnerExample.setDeletesPending(true);
			return;
		}
		TestABISService service = getTestABISService();
		boolean status = this.lockSystem();
		println("Commencing remote delete after " + gracePeriod/1000.0 + " seconds");
		Commons.waitMillis(gracePeriod);
		println("Remote system reset started");
		service.commitSubjectDeletes();
		this.unlockSystem();
		println("Remote system reset complete");
	}
	
	
	public synchronized boolean lockSystem() {
		boolean status;
		if (lockTest ==null) {
			lockTest=this;
			status=true;
		} else {
			status = isMyLock();
		}
		return status;
	}

	public synchronized boolean unlockSystem() {
		boolean status=false;
		if (lockTest != null) {
			if (isMyLock()) {
				lockTest=null;
				status=true;
			}
		}
		return status;
	}
	
	public boolean waitIfLocked() {
		return waitIfLocked(0, 3000);
	}
	
	
	public boolean waitIfLocked(long timeOut, long interval) {
		int maxCount = (int) (timeOut/interval);
		int count=0;
		boolean exit=false;
		while ( isLocked() && !exit) {
			Commons.waitMillis(interval);
			if (maxCount > 0 && count >= maxCount) {
				break;
			}
		}
		return isLocked();
	}
	
	public boolean isLocked() {
		return isLocked(false);
	}
	
	public boolean isLocked(boolean unlessMine) {
		boolean status = false;
		if( this.lockTest != null) {
			if (unlessMine && isMyLock()) {
				status = false;
			} else {
				status = true;
			}
		}
		return status;
	}
	
	protected boolean isMyLock() {
		return lockTest != null && lockTest.equals(this);
	}
	
	public int getConcurrency() {
		return concurrency;
	}

	

	public int getCounter() {
		return counter;
	}

	public void incrementCounter() {
		synchronized(this.domainName) {
			++counter;
		}
	}
	
	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}
	

	public long getStartSubject() {
		return startSubject;
	}

	public void setStartSubject(long startSubject) {
		this.startSubject = startSubject;
	}

	public int getSubjectCount() {
		return subjectCount;
	}

	public void setSubjectCount(int subjectCount) {
		this.subjectCount = subjectCount;
	}
	
	

	public int getIterationCount() {
		return iterationCount;
	}

	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}

	public int getSampleIndex() {
		return sampleIndex;
	}

	public void setSampleIndex(int sampleIndex) {
		this.sampleIndex = sampleIndex;
	}

	public boolean isSimulationMode() {
		return simulationMode;
	}

	public void setSimulationMode(boolean simulationMode) {
		this.simulationMode = simulationMode;
	}

	
	public int getCyclicShift() {
		return cyclicShift;
	}

	public void setCyclicShift(int cyclicShift) {
		this.cyclicShift = cyclicShift;
	}
	
	
	
	public boolean isSetupOnly() {
		return setupOnly;
	}


	public void setSetupOnly(boolean setupOnly) {
		this.setupOnly = setupOnly;
	}


	public String getDomainName() {
		String result = domainName;
		if(this.cyclicShift > 0) {
			result +="_s" + this.cyclicShift;
		}
		return result;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	@Override
	protected void setUp() {
		super.setUp();
	}
	
	Set<String> createdSubjects = CollectionUtils.newSet();
	Set<String> legacyCreateSubjects = CollectionUtils.newSet();
	private Set<String> createdBiographicRecords = CollectionUtils.newSet();	
	
	
	public Set<String> getCreatedSubjects() {
		return createdSubjects;
	}

	public Set<String> getLegacyCreateSubjects() {
		return legacyCreateSubjects;
	}

	public Set<String> getCreatedBiographicRecords() {
		return createdBiographicRecords;
	}
	
	public void notifyCreateSubject(String subject) {
		synchronized(createdSubjects) {
			this.getCreatedSubjects().add(subject);			
		}
	}

	public void notifyDeleteSubject(String subject) {
		synchronized(createdSubjects) {
			this.getCreatedSubjects().remove(subject);
		}
	}
	
	public void notifyCreateLegacySubject(String subject) {
		synchronized(legacyCreateSubjects) {
			this.getLegacyCreateSubjects().add(subject);
		}
	}

	public void notifyDeleteLegacySubject(String subject) {
		synchronized(legacyCreateSubjects) {
			this.getLegacyCreateSubjects().remove(subject);
		}
	}
	
	public void notifyCreateBiographicSubject(String subject) {
		synchronized(createdBiographicRecords) {
			this.getCreatedBiographicRecords().add(subject);
		}
	}
	
	public void notifyDeleteBiographicSubject(String subject) {
		synchronized(createdBiographicRecords) {
			this.getCreatedBiographicRecords().remove(subject);
		}
	}
	

	/**
	 * Core implementation of multi-threaded test
	 * @param taskThread
	 */
	public void executeThreadTest(SubjectProcessorThread taskThread) {
		taskThread.setConcurrencyTest(this);
		taskThread.setDomainName(this.getDomainName());
		taskThread.setSample(this.getSampleIndex());
		taskThread.initializeSubject(this.getStartSubject(), this.getSubjectCount(), this.getIterationCount(), 1);
		ConcurrencyTestRunner runner = new ConcurrencyTestRunner(taskThread, this.getConcurrency());
		runner.start();
		runner.waitStop();
	}
	
	protected void checkSubjectsExist() {
		checkSubjectsPresent(true);
	}

	protected void checkSubjectsNotPresent() {
		int deleteCount = checkSubjectsPresent(false);
		if (deleteCount > 0 ) {
			this.commitABISDeletesSync();
		}
	}
	
	protected int checkSubjectsPresent(boolean status) {
		GenkeyABISService service = ABISServiceModule.getABISService();;
		boolean available = service.testAvailable();
		assertTrue(available);
		String connection = service.testABISConnection();
		assertTrue(connection != null); 
		printObject("ABIS Connection", connection);
		int changeCount=0;

		for(int ix=0; ix < this.getSubjectCount() ; ix++ ) {
			long subject = this.getStartSubject() + ix;
			if (checkSubjectPresent(subject, this.sampleIndex, this.getDomainName(), status)) {
				++changeCount;
			}
		}
		return changeCount;
	}
	
	
	protected boolean checkSubjectPresent(long subject, int sampleIndex, String domainName, boolean status) {
		TestABISService service = this.getTestABISService();
		String externalId = BiographicIdentifier.resolveExternalID(String.valueOf(subject), domainName);
		boolean exists = service.existsSubject(externalId);
		if (status) {
			if (! exists ) {
				println("Creating " + subject + " for " + this.getTestName());
				SubjectEnrollmentReference enrolRef = EnrollmentUtils.accessEnrollmentRecord(subject, sampleIndex, domainName);
				MatchEngineResponse response = service.insertSubject(enrolRef, false);
				assertTrue(response.isSuccess());
				//createdSubjects.add(externalId);
				this.notifyCreateSubject(externalId);
			} else {
				println("Subject " + subject + "/" + domainName + " already exists");
			}
		} else {
			if (exists) {
				service.deleteSubject(externalId, true);
				this.notifyDeleteSubject(externalId);
			} else {
				println("Subject " + subject + "/" + domainName+ " not present");				
			}
		}
		return status != exists;
	}
	
	protected void deleteCreatedSubjects() {
		TestABISService service = getTestABISService();
		int count=0;
		for(String externalId : createdSubjects ) {
			println("Deleting subject " + externalId);
			if (service.deleteSubject(externalId, true)) {
				count++;
			}
		}
		if (count > 0) {
			this.commitABISDeletesSync();
		}
	}
	
	protected void deleteTestSubjectDomain() {
		TestABISService service = getTestABISService();
		String domainName = this.getDomainName();
		service.deleteDomainSubjects(domainName);
	}

	protected int checkLegacySubjectsExist() {
		LegacyMatchingService service = DGIEServiceModule.getLegacyService();
		boolean available = service.testAvailable();
		assertTrue(available);

		int changeCount=0;
		for(int ix=0; ix < this.getSubjectCount() ; ix++ ) {
			long subject = this.getStartSubject() + ix;
			if (checkLegacySubjectPresent(subject, this.sampleIndex, this.getDomainName())) {
				++changeCount;
			}
		}
		return changeCount;
	}


	
	protected boolean checkLegacySubjectPresent(long subject, int sampleIndex, String domainName) {
		LegacyMatchingService service = DGIEServiceModule.getLegacyService();
		String externalId = BiographicIdentifier.resolveExternalID(String.valueOf(subject), domainName);
		boolean status= false;
		if (! service.existsSubject(externalId)) {
			println("Creating " + subject + " for " + this.getTestName());
			SubjectEnrollmentReference enrolRef = EnrollmentUtils.accessEnrollmentRecord(subject, sampleIndex, domainName, this.cyclicShift);
			SubjectEnrollmentReference legacyRef = EnrollmentUtils.restrictFingers(enrolRef, Thumbs);
			int [] fingersPresent = legacyRef.getFingersPresent();
			printResult(subject + " with fingers ", legacyRef.getFingersPresent());
			printResult("SubjectId", enrolRef.getSubjectID());
			printResult("Legacy SubjectId", legacyRef.getSubjectID());
			boolean result = service.registerSubject(legacyRef);
			assertTrue(result);
			//legacyCreateSubjects.add(externalId);
			this.notifyCreateLegacySubject(externalId);
			status=true;
		} else {
			println("Subject " + subject + " already exists");
		}
		return status;
	}
	
	protected void deleteCreatedLegacySubjects() {
		LegacyMatchingService service = DGIEServiceModule.getLegacyService();
		for(String subjectId : legacyCreateSubjects) {
			service.deleteSubject(subjectId);
		}
	}
	
	protected int deleteBiographicSubjects() {
		BiographicService service = BiographicServiceModule.getBiographicService();
		boolean available = service.testAvailable();
		assertTrue(available);

		int count=0;
		for(int ix=0; ix < this.getSubjectCount() ; ix++ ) {
			long subject = this.getStartSubject() + ix;
			String externalId = BiographicIdentifier.resolveExternalID(String.valueOf(subject), getDomainName());
			if (service.existsBiographicRecord(externalId)) {
				count++;
				service.deleteBiographicRecord(externalId);
			}
		}		
		return count;
	}
	
	protected int checkBiographicSubjectsExist() {
		BiographicService service = BiographicServiceModule.getBiographicService();
		boolean available = service.testAvailable();
		if (!available && service.getStatusCode() == RemoteAccessService.ConnectException) {
			int statusCode = service.getStatusCode();
			available = service.checkFailover(false, 0);
		}
		assertTrue(available);
		int count=0;
		for(int ix=0; ix < this.getSubjectCount() ; ix++ ) {
			long subject = this.getStartSubject() + ix;
			if (checkBiographicSubjectPresent(subject, this.getDomainName())) {
				count++;
			}
		}
		return count;
		
		/*
		SubjectListController controller = getTaskController();
		while(controller.hasMoreTasks()) {
			long subject = controller.getNextTaskObject(Long.class);
			checkBiographicSubjectPresent(subject, this.getDomainName());
		}
		*/
	}

	
	
	
	private boolean checkBiographicSubjectPresent(long subject, String domainName) {
		boolean changed=false;
		BiographicService service = BiographicServiceModule.getBiographicService();
		String externalId = BiographicIdentifier.resolveExternalID(String.valueOf(subject), domainName);
		if (! service.existsBiographicRecord(externalId)) {
			println("Creating biographic subject " + externalId);
			BiographicProfileRecord biographicRecord = EnrollmentUtils.getBiographicRecord(externalId, "christian_" + subject, "surname_" + subject);
			boolean status = service.insertBiographicRecord(biographicRecord);
			assertThat(service, serviceCallGood());
			//createdBiographicRecords.add(externalId);
			this.notifyCreateBiographicSubject(externalId);
			changed=true;
		} else {
			println("Subject " + subject + " already exists");	
		}
		return changed;
	}

	public void checkBiographicSubjectsDeleted() {
		BiographicService service = BiographicServiceModule.getBiographicService();
		for(String biographicId : createdBiographicRecords) {
			service.deleteBiographicRecord(biographicId);
		}
	}	
	
	
	protected SubjectListController getTaskController() {
		SubjectListController controller = new SubjectListController();
		controller.setSubjectList(this.startSubject, this.subjectCount, 1);
		return controller;
	}	
	
	
	
	public static class EnrollmentTestThread extends SubjectProcessorThread {


		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			GenkeyABISService service = ABISServiceModule.getABISService();
			String subjectId = ref.getSubjectID();
			boolean exists = service.existsSubject(subjectId);
			assertTrue(service.isSuccess());
			if (exists) {
				super.printMessage("Subject " + subjectId + " is already present");
			} else {
				MatchEngineResponse response = service.insertSubject(ref, false);
				boolean success = response.isSuccess();
				String result = response.getOperationResult();
				printResult("Operation results", result);
				int nMatches = response.getMatchResults().size();
				printResult("NMatches", nMatches);
				this.getConcurrencyTest().notifyCreateSubject(subjectId);				
			}
		}
	}
	
	public static class BiographicInsertOrFetchThread extends SubjectProcessorThread {
		
		
		
		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			BiographicService service = BiographicServiceModule.getBiographicService();
			String subjectId = ref.getSubjectID();
			boolean exists = service.existsBiographicRecord(subjectId);
			assertTrue(service.isSuccess());
			if (exists) {
				super.printMessage("Subject " + subjectId + " is already present");
				BiographicProfileRecord record = service.fetchBiographicRecord(subjectId);
			} else {
				//MatchEngineResponse response = service.insertSubject(ref, false);
				BiographicProfileRecord record = EnrollmentUtils.getBiographicRecord(subjectId, "christian_" + subject, "surname_" + subject);
			
				boolean success = service.insertBiographicRecord(record);
				assertThat(success, is(true));
				assertThat(service, is(serviceCallGood()));
				this.getConcurrencyTest().notifyCreateBiographicSubject(subjectId);
			}
		}
		
		protected BiographicProfileRecord createInsertRecord(String subjectId) {
			long subject = getSubjectNumber(subjectId);
			BiographicService service = BiographicServiceModule.getBiographicService();
			BiographicProfileRecord record = EnrollmentUtils.getBiographicRecord(subjectId, "christian_" + subject, "surname_" + subject);
			
			boolean success = service.insertBiographicRecord(record);
			assertThat(success, is(true));
			assertThat(service, is(serviceCallGood()));	
			this.getConcurrencyTest().notifyCreateBiographicSubject(subjectId);
			
			return record;
		}
		
		protected void deleteIfPresent(String subjectId) {
			BiographicService service = BiographicServiceModule.getBiographicService();
			boolean exists = service.existsBiographicRecord(subjectId);
			assertTrue(service.isSuccess());
			if (exists) {
//				super.printMessage("Subject " + subjectId + " is already present");
				service.deleteBiographicRecord(subjectId);
				this.getConcurrencyTest().notifyDeleteBiographicSubject(subjectId);
			}
		}
		
		static long getSubjectNumber(String externalId) {
			String localId = BiographicIdentifier.localIDOf(externalId);
			return Long.valueOf(localId);
		}
		
		protected void createIfAbsent(String subjectId) {
			BiographicService service = BiographicServiceModule.getBiographicService();
			boolean exists = service.existsBiographicRecord(subjectId);
			assertTrue(service.isSuccess());
			if (!exists) {
				createInsertRecord(subjectId);
			}
		}
		
	}

	public static class BiographicInsertThread extends BiographicInsertOrFetchThread {
		

		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			BiographicService service = BiographicServiceModule.getBiographicService();
			String subjectId = ref.getSubjectID();
			
			deleteIfPresent(subjectId);
			createInsertRecord(subjectId);
		}
	}
	

	public static class BiographicFetchThread extends BiographicInsertOrFetchThread {
		

		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			BiographicService service = BiographicServiceModule.getBiographicService();
			String subjectId = ref.getSubjectID();
			
			createIfAbsent(subjectId);
			BiographicProfileRecord record = service.fetchBiographicRecord(subjectId);
			printMessage("Fetched record " + record.getBiographicID());
		}
	}
	
	
	public static class QueryTestThread extends SubjectProcessorThread {


		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			ref.setSubjectID(null);
			GenkeyABISService service = ABISServiceModule.getABISService();
			MatchEngineResponse response = service.querySubject(ref, false);
			boolean success = response.isSuccess();
			if (success) {
				String result = response.getOperationResult();
				printResult("Operation results", result);
				int nMatches = response.getMatchResults().size();
				printResult("NMatches", nMatches);			
			} else {
				String message = response.getErrorMessage();
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult("Error", message);
			}
		}
		
	}

	
	public static class VerifyTestThread extends SubjectProcessorThread {


		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			GenkeyABISService service = ABISServiceModule.getABISService();
			VerifyResponse response = service.verifySubject(ref);
			boolean success = response.isSuccess();
			if (success) {
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult(subject + " Verified ", response.isVerified());			
			} else {
				String message = response.getErrorMessage();
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult("Error", message);
			}
		}
		
	}

	public static class LegacyVerifyThread extends SubjectProcessorThread {


		@Override
		void executeSubjectTask(SubjectEnrollmentReference enrolRef, long subject, int sample) {
			SubjectEnrollmentReference testRef = EnrollmentUtils.restrictFingers(enrolRef, Thumbs);
			LegacyMatchingService service = DGIEServiceModule.getLegacyService();
			VerifyResponse response = service.verifySubject(testRef);
			boolean success = response.isSuccess();
			if (success) {
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult(subject + " Verified ", response.isVerified());	
				println(subject + " is " + (response.isVerified() ? "verified" : "not verified") + " with score " + response.getMatchResult().getMatchScore());
			} else {
				String message = response.getErrorMessage();
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult("Error", message);
			}
		}		
	}
	
	public static class ImageFetchThread extends SubjectProcessorThread {


		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
			GenkeyABISService service = ABISServiceModule.getABISService();
			//VerifyResponse response = service.verifySubject(ref);
			int [] fingers = PartnerExample.Thumbs;
			ImageRequestResponse response = service.getImageRequest(ref.getSubjectID(), fingers);
			boolean success = response.isSuccess();
			if (success) {
				String result = response.getOperationResult();
				printResult("Operation results", result);
				ImageWrapperSet wrapper = response.getImageWrapper();
				
				int resoution = wrapper.getResolution();
				
				for(int finger : wrapper.keySet()) {
					ImageBlob imageBlob = wrapper.get(finger);
					int resolution = wrapper.getResolution(finger);
					printResult("Image " + finger, imageBlob.getImageEncoding().length + "/" + imageBlob.getImageFormat());
				}
				
				
				
				int [] iFingers = response.getFingers();
				for(int finger : iFingers) {
					ImageBlob imageBlob = response.getImageBlob(finger);
					printResult("Image " + finger, imageBlob.getImageEncoding().length + "/" + imageBlob.getImageFormat());
				}
			} else {
				String message = response.getErrorMessage();
				String result = response.getOperationResult();
				printResult("Operation results", result);
				printResult("Error", message);
			}
		}
		
	}
	
	/**
	 * Implements a mixed workload
	 * @author Gavan
	 *
	 */
	/*
	public static class MultiPlexerThread extends SubjectProcessorThread {

		List<SubjectProcessorThread> processorThreads = new ArrayList<>();
		int index=0;
		
		List<SubjectProcessorThread> completedThreads = new ArrayList<>();
		
		@Override
		protected void executeSubjectTask(long subject) {
			SubjectProcessorThread test = getNextThread();
			if (test == null) {
				this.setFinished(true);
				return;
			}
			
			Long subjectId = test.getTaskController().getNextTaskObject(Long.class);
			if (subjectId != null) {
				test.executeSubjectTask(subjectId);
			} else {
				
			}
		}
		
		boolean checkFinished(SubjectProcessorThread thread) {
			boolean status = thread.isFinished();
			if (thread.isFinished()) {
				this.completedThreads.add(thread);
				this.processorThreads.remove(thread);				
			}
			return status;
		}

		@Override
		void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample) {
		}
		
		synchronized SubjectProcessorThread getNextThread() {
			SubjectProcessorThread thread = getNextThread2();
			while(thread !=null && thread.isFinished()) {
				thread = getNextThread2();
			}
			return thread;
		}
		
		SubjectProcessorThread getNextThread2() {
			if (index >= processorThreads.size()) {
				index=0;
			}
			SubjectProcessorThread test = processorThreads.get(index++);
			return test;
		}
	}
	*/

	public void runStressTest() {
		this.setUp();
		this.stressTest();
		this.tearDown();
	}


	abstract public void stressTest() ;
	
	/*
	protected void doStressTest() {
		throw new RuntimeException("Must implement doStressTest for class");
	}
	*/

	
}
