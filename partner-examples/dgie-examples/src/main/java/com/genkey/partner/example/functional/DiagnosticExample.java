package com.genkey.partner.example.functional;

/*
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
*/

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.StopWatch;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.afis.jaxb.InspectRequest;
import com.genkey.afis.jaxb.InspectResponse;
import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.JAXBMarshallUtils;
import com.genkey.platform.utils.Logging;
import com.genkey.platform.utils.ResourceUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

public class DiagnosticExample extends PartnerExample {

	static final String TestSubject = "94E7D7485A2C4F0AA6F1AC2D693B4CD4";

	static final String ENV_CONFIG_HOME = "IST_CONFIG_HOME";

	javax.ws.rs.client.Client client;
	org.springframework.context.support.GenericApplicationContext context;

	// Client client;

	public static void main(String[] args) {
		DiagnosticExample example = new DiagnosticExample();
		example.runTestExamples();
	}

	public void runAllExamples() {
		jaxbInstantiationTest();
		testConfigAccess();
		// responseParseCheck();
		// imageCheck();
		// testSubjectCheck();
	}

	static String responseResources[] = { "testData/testResponse0.xml", "testData/testResponse1.xml" };

	public void jaxbInstantiationTest() {
		try {
			JAXBTestObject object = new JAXBTestObject("gavan", 62);
			String xml = JAXBMarshallUtils.marshallXML(object);
			JAXBTestObject objectRb = JAXBMarshallUtils.unmarshallXML2(JAXBTestObject.class, xml);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void testJAXB() {
		try {
			JAXBContext jbx = JAXBContext.newInstance(InspectRequest.class);
			Unmarshaller unmarshaller = jbx.createUnmarshaller();

		} catch (Exception e) {
			handleException(e);
		}
	}

	public void testConfigAccess() {

		String configPath = FileUtils.getConfigurationPath();
		boolean exists;

		super.printHeader("Running configuration test");

		String envValue = System.getenv(ENV_CONFIG_HOME);
		super.printResult(ENV_CONFIG_HOME, envValue);

		if (configPath == null) {
			printError("Configuration path not configured");
		} else {
			printMessageF("Configuration path is configured as %s", configPath);
			exists = FileUtils.isDirectory(configPath);
			if (!exists) {
				printErrorF("Configuration path %s configured does not exist", configPath);
			} else {
				printMessageF("Configuration path exists");
			}
		}
		String imageFolder = TestDataManager.getImageDirectory();
		if (FileUtils.isDirectory(imageFolder)) {
			printMessageF("ImageFolder %s exists", imageFolder);
		} else {
			printErrorF("Imagefolder %s does not exist", imageFolder);
		}

		String testImageFile = TestDataManager.getImageFile(1, 1, 1);
		boolean loadSuccess;
		if (FileUtils.existsFile(testImageFile)) {
			printMessageF("Image file %s for subject/finger/sample %d/%d/%d exists", testImageFile, 1, 1, 1);
			try {
				ImageData image = TestDataManager.loadImage(testImageFile);
				loadSuccess = (image != null);
			} catch (Exception e) {
				loadSuccess = false;
			}
		} else {
			printErrorF("Image file %s for subject/finger/sample %d/%d/%d not found ", testImageFile, 1, 1, 1);
			loadSuccess = false;
		}
		if (loadSuccess) {
			printMessageF("TestImage %s successfully loaded", testImageFile);
		} else {
			printErrorF("TestImage %s successfully loaded", testImageFile);
		}

		printEndTest("testConfigAccess");
	}

	public void responseParseCheck() {
		for (String resource : responseResources) {
			try {
				String xmlContent = ResourceUtils.getResourceAsText(resource);
				InspectResponse response = JAXBMarshallUtils.unmarshallXML2(InspectResponse.class, xmlContent);
				String exportXML = JAXBMarshallUtils.marshallXML(response, true);
			} catch (Exception e) {
				String errorStack = Logging.format(e);
				FormatUtils.println(errorStack);
			}
		}
	}

	public void testSubjectCheck() {

		GenkeyABISService service = super.getABISService();
		StopWatch watch = new StopWatch();
		if (service.testAvailable()) {
			printTimerResult("Available", watch);
			StopWatch watch2 = new StopWatch();
			boolean status = service.existsSubject(TestSubject);
			printTimerResult("Exists", watch2);
		}
		printTimerResult("Total", watch);
		super.printEndTest("Timer test");
	}

	static final String imageFile = "./replay/kojak_image.bmp";

	public void imageCheck() {
		String format = TestDataManager.getImageFormat();
		TestDataManager.setFingerImagePath(4);
		String imagePath = TestDataManager.getImageDirectory();
		int[] fingers = { 10, 9, 8, 7 };
		String[] fileNames = FileUtils.getFilenames(imagePath, true);
		String path = this.getTestPath("segments");
		for (String fileName : fileNames) {
			String baseName = FileUtils.baseName(fileName);
			String exportFile = FileUtils.expandConfigFile(path, baseName, ImageUtils.EXT_BMP);
			ImageData image = TestDataManager.loadImage(fileName);
			ImageContext context = new ImageContext(image, fingers);
			for (int ix = 0; ix < context.count(); ix++) {
				String segmentFile = FileUtils.extendBaseName(exportFile, String.valueOf(ix));
				ImageData segment = context.extractImageSegment(ix, true);
				// ImageUtils.showImage(segment);
				byte[] encoding = segment.asEncodedImage(format);
				try {
					FileUtils.byteArrayToFile(encoding, segmentFile);
				} catch (Exception e) {
					handleTestException(e);
				}
			}
		}
	}

	static final String TestSubject1 = "72A749BC-ECE2-374C-AD04-A0FB600906A5";
	static final String TestSubject2 = "E7B6F62D-2267-404B-A665-DF1185E97930";

	public void testDeleteSubject() {
		GenkeyABISService service = super.getABISService();
		boolean b1 = service.existsSubject(TestSubject1);
		boolean b2 = service.existsSubject(TestSubject2);
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "object")
	public static class JAXBTestObject {
		@XmlAttribute(name = "name")
		String name;

		@XmlAttribute(name = "age")
		int age;

		public JAXBTestObject() {

		}

		public JAXBTestObject(String name, int age) {
			super();
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	};
	
}
