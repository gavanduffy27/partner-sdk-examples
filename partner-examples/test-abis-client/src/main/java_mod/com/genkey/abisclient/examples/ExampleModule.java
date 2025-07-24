package com.genkey.abisclient.examples;

import java.util.List;

import com.genkey.abisclient.EnrolmentSettings;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.QualityInfo.QualityRank;
import com.genkey.abisclient.examples.utils.ScannerDialogs;
import com.genkey.abisclient.examples.utils.TestConfig;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.platform.utils.ArrayIterator;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.PropertyMap;

/**
 * Base class for example program modules that can be combined within an overall
 * instance ExampleSuite.
 * @author gavan
 *
 */
public abstract class ExampleModule implements IExampleTest{

	protected static final String SingleFingerDirectory = "images/ABISClientBMP/SingleFinger";
	protected static final String TwoFingerDirectory = "images/ABISClientBMP/TwoFinger";
	protected static final String FourFingerDirectory = "images/ABISClientBMP/FourFinger";

	protected static final int [] FourFingers = {3,4,7,8};
	protected static final int [] RightHandFingers = {2,3,4,5};
	protected static final int [] LeftHandFingers = {7,8,9, 10};
	protected static final int [] Thumbs = {1,6};
	protected static final int [] UnknownFingerList = {0};
	
	
    String previousDirectory=null;

	
    static PropertyMap propertyMap = new PropertyMap();
    
    public static boolean UseGui=false;

    static UserMessageHandler DefaultMessageHandler = new DefaultUserMessageHandler();
 
    
    static UserMessageHandler messageHandler = DefaultMessageHandler;
    
    
    TestState testState = null;
    
    public static class TestState
    {
    	String imageDirectory;
    	String imageFormat;
    	EnrolmentSettings enrolmentSettings = new EnrolmentSettings();
    	
    	MatchEngineConfiguration.Settings matchEngineSettings = MatchEngineConfiguration.getInstance().getSettings();
    	
    	public TestState(boolean loadSync) {
    			if (loadSync) {
    				this.loadSync();
    			}
    	}
    	
    	public void loadSync() {
    		imageDirectory = TestDataManager.getImageDirectory();
    		imageFormat = TestDataManager.getImageFormat();
    		this.enrolmentSettings.syncLoad();
    		this.matchEngineSettings = MatchEngineConfiguration.getInstance().getSettings();
    		this.matchEngineSettings.loadSync();
    	}
    	
    	public void applyDataManagerSettings() {
    		TestDataManager.setImageDirectory(imageDirectory);
    		TestDataManager.setImageFormat(imageFormat);
    	}
    	
    	public void applyConfigurationSettings() {
    		this.enrolmentSettings.applySettings();
    		this.matchEngineSettings.apply();
    	}
    	
    	public void applyAll() {
    		applyDataManagerSettings();
    		applyConfigurationSettings();
    	}
    	
    };
    
    
    
    
	protected void setUp() {
		//previousDirectory = TestDataManager.getImageDirectory();
		//ImageContextSDK.resetConfiguration();
		testState = new TestState(true);
	}
	
	protected void tearDown() {
		//ImageContextSDK.resetConfiguration();
		//TestDataManager.setImageDirectory(previousDirectory);
		testState.applyAll();
	}
	
	
	
	public boolean isUseGui() {
		return UseGui;
	}

	public void setUseGui(boolean useGui) {
		ExampleModule.UseGui = useGui;
	}
	
	
	public static boolean isUseLegacy() {
		return TestConfig.getInstance().isUseLegacy();
	}

	public static UserMessageHandler getDefaultMessageHandler() {
		return DefaultMessageHandler;
	}

	
	
	public static PropertyMap getPropertyMap() {
		if (propertyMap == null) {
			propertyMap = TestConfig.getInstance().getProperties();
		}
		return propertyMap;
	}

	/**
	 * Overrides the default message-handler to use a preferred implementation as the default for
	 * all classes. 
	 * <p>
	 * In general this is the only call required, however the handler can be specialised to be different
	 * for different example modules, without affecting the global default.
	 * @param defaultMessageHandler
	 */
	public static void setDefaultMessageHandler(UserMessageHandler defaultMessageHandler) {
		DefaultMessageHandler = defaultMessageHandler;
	}

	@Override
	public UserMessageHandler getUserMessageHandler() {
		return messageHandler;
	}

	public void setUserMessageHandler(UserMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void runTestExamples() {
		printHeader("Running test examples for " + Commons.classShortName(this));
		setUp();
		runAllExamples();
		tearDown();
	}

	abstract protected void runAllExamples(); 
	
	
	protected boolean promptOperatorContinue(String errorDescription) {
		return this.getUserMessageHandler().promptContinue(errorDescription);
	}

	/*
	protected static boolean promptOperatorContinue(String errorDescription, boolean useGui) {
		r
		String message = "ERROR:\n" + errorDescription;
		if (useGui) {
			return SwingDialogs.promptContinue(message, "Operator prompt");
		} else {
			return ScannerDialogs.promptContinue(message, "Operator prompt");
		}
	}
	*/
	
	public void doFeedback(QualityInfo qInfo, int index) {
		printHeader("Quality feedback for image " + index + " with rank " + qInfo.getRank());
		
		if (qInfo.getCoverage() == 100) {
			printMessage("Full coverage with quality score "+ qInfo.getQualityScore());
		} 
		if (qInfo.getRotation() !=0) {
			if (qInfo.getRotation() < 0) {
				printMessage("Finger is rotated to the left, roll clockwise");
            } else {
                printMessage("Finger is rotated to the right, roll anticlockwise");
            }
        }

		if (qInfo.getTilt() != 0) {
			if (qInfo.getTilt() < 0) {
				// This may indicate either that the finger is tilted, in which
				// case there is usually white space at base of image
				printMessage("Finger is tilted away from scanner, place finger horizontally on scanner");
				// OR it indicates that the finger is placed so that tip is in
				// the middle of platen such that core appears near
				// base of scanner. in which case there is white space at the
				// top of the image.
				printMessage("Or finger needs to be placed further forward so that tip is towards top edge of scanner");
			} else {
				// Not actually possible to tilt finger in other direction, so
				// this will almost never happen
				// Positive tilt meaning core is towards base of image, usually
				// indicates finger is overreaching end of scanner
				printMessage("Finger is too far forward beyond end of scanner, move finger so tip is within edge of scanner");
			}
		}
		
		if (qInfo.getRank().ordinal() < QualityRank.MEDIUM.ordinal()) {
			printMessage("This image is low quality and unsuitable for enrolment");
		}
	}
	
	
	public void printMessage(String text) {
		//System.out.println(text);
		this.getUserMessageHandler().printMessage(text);
	}
	
	public void printHeader(String text) {
		this.getUserMessageHandler().printHeader(text);
	}

	public void printHeader(String text, char ch) {
		this.getUserMessageHandler().printHeader(text, ch);
	}

	public void printTimerResult(String result, int index, StopWatch timer) {
		printTimerResult(indexName(result, index), timer);
	}
		
	
	public void printTimerResult(String result, StopWatch timer) {
		printResult("Time " + result + " (ms)", timer.getDurationMs());
	}
	
	public void printIndexResult(String variable, int index, Object value) {
		printResult(indexName(variable, index), value);
	}
		
	private static String indexName(String name, int index) {
		return name + "[" + index + "]";
	}

	
	
	public void printHeaderResult(String variable, Object value) {
		printHeader(variable);
		printMessage(value.toString());
	}
	
	public static void printResult(String variable, Object value) {
		PrintMessage(variable + " = " + value.toString());
	}
	
	public void printObject(String header, Object object) {
		printHeader(header);
		printMessage(object.toString());
	}
	
	public static String printArray(int [] values) {
		return CollectionUtils.containerToString(new ArrayIterator<Integer>(values));
	}
	
	
	protected static void handleException(Exception e) {
		throw new RuntimeException(e);
	}

	public static void PrintHeader(String text) {
		getDefaultMessageHandler().printHeader(text);
	}
	
	
	/**
	 * Static equivalent of PrintMessage which can be invoked statically to use the 
	 * default handler.
	 * @param message
	 */
	public static void PrintMessage(String message) {
		getDefaultMessageHandler().printMessage(message);
	}

	public void showImageList(List<ImageData> imageList, String title, double scaleFactor) {
		//BufferedImage bigImage = ImageUtils.createImageGrid(imageList, 0, 0, scaleFactor);
		///getDefaultMessageHandler().showImageFeedback(bigImage, title);
		getDefaultMessageHandler().showImageList(title, imageList, 0, 0, scaleFactor);
	}

}
