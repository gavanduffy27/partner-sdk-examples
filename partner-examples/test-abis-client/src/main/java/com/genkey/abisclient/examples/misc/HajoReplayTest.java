package com.genkey.abisclient.examples.misc;

import java.util.List;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.Enums.GKThresholdParameter;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;

import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceExtractor;
import com.genkey.platform.utils.ArrayIterator;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.Profiler;

/**
 * Contains some custom replay tests that are not part of formal testing.
 * @author Gavan
 *
 */
public class HajoReplayTest  extends ExampleModule {
	
	double cachedSobelThreshold=122;
	
	
	public static void main(String[] args) {
		HajoReplayTest test = new HajoReplayTest();
		test.runTestExamples();
	}
	

	@Override
	protected void runAllExamples() {
		soakTestBiohashInkSamplesMT();
		//soakTestBiohashInkSamples();
	}
	
	
	

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
		super.setUp();
		int version = ABISClientLibrary.getActiveVersion();
		int version2 = version ==4 ? 8 : 4;
		// Activate other version and make it the default
		ABISClientLibrary.activateVersion(version2, true);
		// Switch of fuzzy image rejections
		this.cachedSobelThreshold = ImageContextSDK.getThreshold(GKThresholdParameter.FocusThreshold);
		ImageContextSDK.setThreshold(GKThresholdParameter.FocusThreshold, 0);
		double newValue = ImageContextSDK.getThreshold(GKThresholdParameter.FocusThreshold);
		printMessage("Sobel threshold is now set to " + newValue);
		
		setHighConcurrency();
		
	}


	@Override
	protected void tearDown() {
		// TODO Auto-generated method stub
		super.tearDown();
		ImageContextSDK.setThreshold(GKThresholdParameter.FocusThreshold, cachedSobelThreshold);
	}

	
	/**
	 * Runnable instance for use within long running Soak tests  that perform looped multi-threaded
	 * tests on a main test method.
	 * 
	 * @author Gavan
	 *
	 */
	public static class  SoakTestThread implements Runnable {
		
		HajoReplayTest parentTest;
		
		static int s_id=0;
		
		int loopCount=10;
		
		int id;
		
		boolean complete=false;
		
		public SoakTestThread(HajoReplayTest test, int loopCount) {
			parentTest = test;
			id=nextId();
			this.loopCount = loopCount;
		}

		static synchronized private int nextId() {
			return s_id++;
		}

		@Override
		public void run() {
			for(int ix=0; ix < loopCount && !isComplete() ; ix++) {
				FormatUtils.printHeader( Thread.currentThread().getName() +"::Testing loop " + ix);
				parentTest.biohashReplayExample();
			}
			this.complete=true;
		}
		
		public void setComplete() {
			this.complete=true;
		}
		
		public boolean isComplete() {
			return this.complete;
		}
		public String getName() {
			return Commons.classShortName(this) + "_" + this.id;
		}
		
	}
	
	
	public void soakTestBiohashInkSamplesMT() {
		soakTestBiohashInkSamplesMT(3, 300, 150);
		printHeader("All tests complete");
	}
	
	public void soakTestBiohashInkSamplesMT(int concurrency, int loopCount, double maxMinutes) {
		List<Thread> threads = CollectionUtils.newList();
		List<SoakTestThread> soakThreads = CollectionUtils.newList();
		
		for(int ix=0 ; ix < concurrency; ix++) {
			SoakTestThread runnable = new SoakTestThread(this, loopCount);
			Thread thread = new Thread(runnable);
			String threadName = runnable.getName(); 
			thread.setName(threadName);
			threads.add(new Thread(runnable));
			soakThreads.add(runnable);
		}
		for(Thread thread : threads) {
			thread.start();
		}
		waitThreadsStop(soakThreads, 60000, maxMinutes);
		
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void waitThreadsStop(List<SoakTestThread> threads, long reportInterval, double maxMinutes) {
		long nextInterval = reportInterval;
		boolean running=true;
		Profiler.TimeStamp timer = Profiler.getTimer();
		Profiler.TimeStamp intervalTimer = Profiler.getTimer();
		while(running) {
			for(SoakTestThread thread : threads) {
				while (!thread.isComplete() && running) {
					Commons.waitMillis(5000);
					if(intervalTimer.getDurationMillis() > nextInterval) {
						double lapsedTimeMins = timer.getLapsedTimeMilli()/60000;
						if (lapsedTimeMins > maxMinutes) {
							thread.setComplete();
							FormatUtils.printBanner("Thread notified for exit on timeout at " + lapsedTimeMins + " minutes",'$');							
						} else {
							FormatUtils.printBanner( thread.getName() + " Interval timer after " + lapsedTimeMins + " minutes", '#');
							intervalTimer = Profiler.getTimer();
							long delay = (long) timer.getDurationMillis();
							long excess = delay % reportInterval;
							nextInterval = reportInterval -excess;
						}
					}
				}
				
				FormatUtils.printBanner("Thread complete " + thread.getName(),'$');
						}
			running=false;
		}
		FormatUtils.printBanner("All threads complete");
	}


	public void soakTestBiohashInkSamples() {
		soakTestBiohashInkSamples(100);
	}
	
	public void soakTestBiohashInkSamples(int loopCount) {
		for(int ix=0; ix < loopCount ; ix++) {
			printHeader( Thread.currentThread().getName() +"::Testing loop " + ix);
			biohashReplayExample();
		}
	}


	public void biohashReplayExample() {
		String path1 = FileUtils.expandConfigPath("replay/hajo/problem2/crash1");
		String path2 = FileUtils.expandConfigPath("replay/hajo/problem2/crash2");
		biohashReplayExample(path1);
		biohashReplayExample(path2);		
	}
	

	public void biohashReplayExample(String dirName) {
		String format = "wsq";
		String path = FileUtils.expandConfigPath(dirName);
		String []fileNames = FileUtils.getFilenames(path, format, true);
		List<ReferenceDataItem> referenceList = CollectionUtils.newList();

		boolean skipErrors=true;
		for(String fileName : fileNames) {
			int []fingers = inferFingers(fileName);
			try {
				byte [] encoding = FileUtils.byteArrayFromFile(fileName);
				ImageData imageData = new ImageData(encoding, format);
				//imageData.setResolution(600);		
	
				ImageContext context = new ImageContext(imageData, fingers);
				int status = context.getStatus();
				if (ImageContextSDK.isBlockingError(status)) {
					String errorDesc = ImageContextSDK.getErrorDescription(status);
					printMessage("Skipping file " + fileName + " with error " + errorDesc);
					if (skipErrors) {
						continue;
					}
				}

				for (int ix = 0; ix < context.count(); ix++) {
					ReferenceDataItem reference = context.getReferenceData(ix);
					referenceList.add(reference);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (referenceList.size() > 0) {
			String threadName = Thread.currentThread().getName();
			AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();
			AnonymousFusionReference fusionReference = extractor.createFromReferences(referenceList, true, null);
			//printObject("FusionReference Summary", fusionReference);
			FormatUtils.printBanner(threadName + "::" + "FusionReference created for " 
					+ CollectionUtils.containerToString(new ArrayIterator<Integer>(fusionReference.getFingers()),"," )
				+ " for " + FileUtils.baseName(dirName));
			//printObject("FusionReference Full State", fusionReference.toString());
		}
		
	}

	static int [] inferFingers(String fileName) {
		int [] result ;
		
		String baseName = FileUtils.baseName(fileName.toLowerCase());
		
		if (baseName.indexOf("lefthand") >= 0) {
			result = LeftHandFingers;
		} else if (baseName.indexOf("righthand") >= 0) {
			result = RightHandFingers;
		} else if (baseName.indexOf("thumbs") >= 0) {
			result = Thumbs;
		} else {
			result = null;
		}
		
		return result;
	}
	
	
}
