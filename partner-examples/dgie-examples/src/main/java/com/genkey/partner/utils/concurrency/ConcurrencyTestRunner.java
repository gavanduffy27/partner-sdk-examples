package com.genkey.partner.utils.concurrency;

import java.util.ArrayList;
import java.util.List;

import com.genkey.platform.utils.Commons;

/**
 * Mechanism for running concurrency tests on  a ConcurrentTaskThread
 * 
 * @author Gavan
 *
 */
public class ConcurrencyTestRunner {
	
	
	
	List<ConcurrentTaskThread> runThreads=null;
	
	List<Thread> threadList=null;
	
	
	public ConcurrencyTestRunner() {
		
	}
	
	public ConcurrencyTestRunner(ConcurrentTaskThread thread, int nThreads) {
		prepareTest(thread, nThreads);
	}
	
	/**
	 * Prepares this as a test runner for specified ConcurrentTaskThread and specified concurrency.
	 * @param thread
	 * @param nThreads
	 */
	public void prepareTest(ConcurrentTaskThread thread, int nThreads) {
		runThreads = new ArrayList<>();
		threadList = new ArrayList<>();
		for(int ix=0; ix < nThreads; ix++) {
			ConcurrentTaskThread runThread = thread.generateTaskThread();
			runThread.setThreadID(ix);
			runThreads.add(runThread);
			threadList.add(new Thread(runThread));
		}
	}
	
	public void start() {
		for(Thread thread : this.threadList) {
			thread.start();
		}
	}
	
	public void stop() {
		for(ConcurrentTaskThread task : this.runThreads) {
			task.stop();
		}
	}
	
	public boolean checkFinished() {
		boolean status = true;
		for(ConcurrentTaskThread task : this.runThreads) {
			if (! task.isFinished()) {
				status=false;
				break;
			}
		}
		return status;
	}
	
	public void waitStop() {
		for(ConcurrentTaskThread task : this.runThreads) {
			waitFinish(task);
		}
		for(Thread thread : this.threadList) {
			try {
				thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public void waitFinish(ConcurrentTaskThread task) {
		while (! task.isFinished()) {
			Commons.waitMillis(1000);
		}
	}

}
