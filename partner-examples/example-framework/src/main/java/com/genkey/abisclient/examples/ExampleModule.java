package com.genkey.abisclient.examples;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.EnrolmentSettings;
import com.genkey.abisclient.Enums.GKSwitchParameter;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.QualityInfo.QualityRank;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.ScannerDialogs;
import com.genkey.abisclient.examples.utils.TestConfig;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.abisclient.verification.VerificationEnums.BioHASHGenerationMode;
import com.genkey.abisclient.verification.VerificationSDK;
import com.genkey.platform.test.framework.GKTestCase;
import com.genkey.platform.test.framework.GKTestCase4;
import com.genkey.platform.utils.ArrayIterator;
import com.genkey.platform.utils.AssertUtils;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.PropertyMap;
import com.genkey.platform.utils.ReflectUtils;

/**
 * Base class for example program modules that can be combined within an overall
 * instance ExampleSuite.
 * @author gavan
 *
 */
public abstract class ExampleModule extends GKTestCase4 implements IExampleTest{

	protected static final String IMAGE_ROOT_PATH="images/ABISClientBMP";

	protected static final String TESTFILE_ROOT_PATH="enrollmentSdk/java";
	
	
	protected static final String SingleFingerDirectory = ImageSetPath("SingleFinger"); // "images/ABISClientBMP/SingleFinger";
	protected static final String TwoFingerDirectory = ImageSetPath("TwoFinger"); //"images/ABISClientBMP/TwoFinger";
	protected static final String FourFingerDirectory = ImageSetPath("FourFinger"); //"images/ABISClientBMP/FourFinger";
	
	

	protected static final int [] FourFingers = {3,4,7,8};
	protected static final int [] RightHandFingers = {2,3,4,5};
	protected static final int [] LeftHandFingers = {7,8,9, 10};
	protected static final int [] Thumbs = {1,6};
	protected static final int [] UnknownFingerList = {0};
	
	protected static final int [] SingleSample = {1};
	protected static final int [] TwoSamples = {1,2};
	protected static final int [] ThreeSamples = {1,2,3};
	
	
    public static String V1Left = "v1-left";
    public static String V2Left = "v2-left";
	
	
    String previousDirectory=null;

    protected static String ImageSetPath(String setName) {
    	return FileUtils.mkFilePath(IMAGE_ROOT_PATH, setName);
    }
    
	
    static PropertyMap propertyMap = new PropertyMap();
    
    public static boolean UseGui=false;

    static UserMessageHandler DefaultMessageHandler = new DefaultUserMessageHandler();
 
    
    static UserMessageHandler messageHandler = DefaultMessageHandler;
    
    String moduleName;	
    
    
    public ExampleModule() {
    	this("ExampleMode");
    }
    
    public ExampleModule(String name) {
    	this.setModuleName(name);
    }
    
    
    @BeforeClass
    public static void unitTestBefore() {
    }
    
    
    public void processCommandLine(String [] args) {
    	if (args.length == 1) {
    		runTestAsCommand(args[0]);
    	} else {
    		this.runTestExamples();
    	}
    }
    
    /**
     * Runs specified command as a test
     * @param methodName
     */
    public void runTestAsCommand(String methodName) {
    	FormatUtils.printBanner("Running test " + methodName + " as a command");
    	this.runSetUp4();
    	try {
    		ReflectUtils.invokePublicMethod(this, methodName);
    	} catch (Exception e) {
    		e.printStackTrace();    		
    	}
    	this.runTearDown4();
    	FormatUtils.printBanner("Test " + methodName + " complete");
    }
    
    public static void runTestAsCommand(Object testClass, String methodName) {
    	FormatUtils.printBanner("Running test " + methodName + " as a command");
    	try {
    		ReflectUtils.invokePublicMethod(testClass, methodName);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	FormatUtils.printBanner("Test " + methodName + " complete");
    }
	 
    
    TestState testState = null;
    
    public static class TestState
    {
    	String imageDirectory;
    	String imageFormat;
    	EnrolmentSettings enrolmentSettings = EnrolmentSettings.newInstance();
    	
    	MatchEngineConfiguration.Settings matchEngineSettings = MatchEngineConfiguration.getInstance().getSettings();
    	
    	BioHASHGenerationMode biohashMode = null;
    	
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
    		this.biohashMode=VerificationSDK.getBioHASHGenerationMode();
    	}
    	
    	public void applyDataManagerSettings() {
    		TestDataManager.setImageDirectory(imageDirectory);
    		TestDataManager.setImageFormat(imageFormat);
    	}
    	
    	public void applyConfigurationSettings() {
    		this.enrolmentSettings.applySettings();
    		this.matchEngineSettings.apply();
    		VerificationSDK.setBioHASHGenerationMode(this.biohashMode);
    	}
    	
    	public void applyAll() {
    		applyDataManagerSettings();
    		applyConfigurationSettings();
    	}
    	
    };
    
    
    
    @Before
    public void runSetUp4() {
    	setUp();
    }
    
    @After
    public void runTearDown4() {
    	tearDown();
    }
    
	protected void setUp() {
		testState = new TestState(true);
	}
	
	protected void tearDown() {
		if (testState != null) {
			testState.applyAll();			
		}
	}
	
	public String getModuleName() {
		if (moduleName == null) {
			moduleName = Commons.classShortName(this);
		}
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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
	
	 public static String getCategoryImageFile(String path, String baseName) {
		 return getCategoryImageFile(path, baseName, ImageData.FORMAT_BMP);
	 }

    public static String getCategoryImageFile(String path, String baseName, String format)
    {
        return FileUtils.expandConfigFile(path, baseName, format);
    }

    public static String getFourFingerImageFile(String name)
    {
        return getCategoryImageFile(FourFingerDirectory, name);
    }

    public String getTwoFingerImageFile(String name)
    {
        return getCategoryImageFile(TwoFingerDirectory, name);
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
	
	
	public static void printMessage(String text) {
		//System.out.println(text);
		DefaultMessageHandler.printMessage(text);
	}
	
	public static void printHeader(String text) {
		DefaultMessageHandler.printHeader(text);
	}

	public static void printHeader(String text, char ch) {
		DefaultMessageHandler.printHeader(text, ch);
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
		PrintMessage(variable + " = " + formatObject(value));
	}

	
	private static String formatObject(Object value) {
		String result;
		if (value == null) {
			result = "(null)";
		} else if (value instanceof List) {
			result = CollectionUtils.containerToString((List<?>) value);
		} else if (Commons.isArray(value)) {
			List<?> list = Arrays.asList(value);
			result = formatObject(list);
		} else {
			result = value.toString();
		}
		return result;
	}

	public static void printObject(String header, Object object) {
		printHeader(header);
		printMessage(formatObject(object));
	}
	
	public static String printArray(int [] values) {
		return CollectionUtils.containerToString(new ArrayIterator<Integer>(values));
	}
	
	
	public static void handleException(Exception e) {
		handleException(e, false);
	}

	public static void handleException(Exception e, boolean throwMe) {
		if (throwMe) {
			throw new RuntimeException(e);
		} else {
			e.printStackTrace();
		}
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
	
	public static void setHighConcurrency() {
		ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadReferenceExtract, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsQualityAssess, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsReferenceExtract, true);
	}
	
	public static void setSimpleSingleThreaded() {
		ImageContextSDK.setSwitch(GKSwitchParameter.AutoExtractReference, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.AutoQualityAssess, true);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadQualityAssess, false);
		ImageContextSDK.setSwitch(GKSwitchParameter.BackgroundThreadReferenceExtract, false);
		ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsQualityAssess, false);
		ImageContextSDK.setSwitch(GKSwitchParameter.MultipleThreadsReferenceExtract, false);		
	}
	
	public void saveImage(ImageData image, String baseName) {
		saveImage(image, baseName, false);
	}
	
	public void saveImage(ImageData image, String baseName, boolean useTs) {
		saveImage(image, baseName, ImageData.FORMAT_BMP, useTs);
	}
	
	public void saveImage(ImageData image, String baseName, String format, boolean useTs) {
		String testFile = getTestFile(baseName, format, useTs);
		byte [] encoding = image.asEncodedImage(format);
		try {
			FileUtils.byteArrayToFile(encoding, testFile, false);
		} catch (Exception e) 
		{
			handleException(e);
		}
	}

	public String getTestFile(String fileName, String ext, boolean applyTimeStamp) {
		String moduleName = getModuleName();
		return getTestFile(moduleName, fileName, ext, applyTimeStamp);
	}
	
	public String getTestFile(String fileName, String ext) {
		return getTestFile(fileName, ext, false);
	}

	public String getTestFile(String module, String fileName, String ext) {
		return getTestFile(module, fileName, ext, false);
	}
	
	public String getTestFile(String module, String fileName, String ext, boolean applyTimeStamp) {
		String baseName = fileName;
		if (applyTimeStamp) {
			baseName = fileName + "_" + System.currentTimeMillis();
		}
		String rootPath = FileUtils.mkFilePath(TESTFILE_ROOT_PATH, module);
		return FileUtils.expandConfigFile(rootPath, baseName, ext);
	}
	
	
	public String getTestPath(String pathName) {
		return getTestPath(this.getModuleName(), pathName);
	}
	
	public String getTestPath(String module, String pathName) {
		String rootPath = FileUtils.mkFilePath(TESTFILE_ROOT_PATH, module);
		return 	FileUtils.mkFilePath(rootPath, pathName);	
	}
	
	/*
	protected static void assertTrue(boolean predicate) {
		AssertUtils.assertTrue(predicate);
	}

	protected static void assertFalse(boolean predicate) {
		AssertUtils.assertTrue(!predicate);
	}
	*/
	
}
