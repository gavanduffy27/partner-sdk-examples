package com.genkey.partner.example.functional;

import java.io.IOException;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.StopWatch;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.afis.jaxb.InspectResponse;
import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.JAXBMarshallUtils;
import com.genkey.platform.utils.Logging;
import com.genkey.platform.utils.ResourceUtils;

public class DiagnosticExample extends PartnerExample{
	
	static final String TestSubject = "94E7D7485A2C4F0AA6F1AC2D693B4CD4";
	
	javax.ws.rs.client.Client client;
	org.springframework.context.support.GenericApplicationContext context;
	
	//Client client;
	
	public static void main(String [] args) {
		DiagnosticExample example = new DiagnosticExample();
		example.runTestExamples();
	}

	public void runAllExamples() {
		responseParseCheck();
//		imageCheck();
//		testSubjectCheck();
	}

	
	static String responseResources[] = {
		"testData/testResponse0.xml",
		"testData/testResponse1.xml"
	};
	
	public void responseParseCheck() {
		for(String resource : responseResources) {
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
	
	
	final static String imageFile = "./replay/kojak_image.bmp";
	
	public void imageCheck()  {
		String format = TestDataManager.getImageFormat();
		TestDataManager.setFingerImagePath(4);
		String imagePath = TestDataManager.getImageDirectory();
		int [] fingers = {10,9,8,7};
		String [] fileNames = FileUtils.getFilenames(imagePath,true);
		String path =  this.getTestPath("segments");
		for(String fileName : fileNames) {
			String baseName = FileUtils.baseName(fileName);
			String exportFile = FileUtils.expandConfigFile(path, baseName, ImageUtils.EXT_BMP);
			ImageData image = TestDataManager.loadImage(fileName);
			ImageContext context = new ImageContext(image, fingers);
			for(int ix=0; ix < context.count(); ix++) {
				String segmentFile = FileUtils.extendBaseName(exportFile, String.valueOf(ix));
				ImageData segment = context.extractImageSegment(ix, true);
				//ImageUtils.showImage(segment);
				byte [] encoding = segment.asEncodedImage(format);
				try {
					FileUtils.byteArrayToFile(encoding, segmentFile);					
				} catch (Exception e) {
					handleTestException(e);
				}
			}
		}
	}
	
	static final  String  TestSubject1 = "72A749BC-ECE2-374C-AD04-A0FB600906A5";
	static final  String  TestSubject2 = "E7B6F62D-2267-404B-A665-DF1185E97930";
	public void testDeleteSubject() {
		GenkeyABISService service = super.getABISService();
		boolean b1 = service.existsSubject(TestSubject1);
		boolean b2= service.existsSubject(TestSubject2);
	}
	
}
