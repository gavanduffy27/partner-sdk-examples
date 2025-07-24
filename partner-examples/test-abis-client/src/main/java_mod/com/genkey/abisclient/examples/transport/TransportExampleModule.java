package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.ReferenceDataItem;

import java.util.List;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;


public abstract class TransportExampleModule  extends ExampleModule{
	
	static String ImageFormat = "wsq";
	
	static int ImageResolution=500;
	

	FingerEnrollmentReference generateFingerBlob(int subjectId, int finger, int sampleIndex, boolean useEncodedImage, boolean autoExtract) {
		// Access the image file
		String imageFile = TestDataManager.getImageFile(subjectId, finger, sampleIndex);

		// Create an instance of standard image data .
		ImageData imageData = TestDataManager.loadImage(imageFile);
		imageData.setResolution(ImageResolution);
		
		// Create the instance for specified finger. 
		FingerEnrollmentReference fingerRef =  new FingerEnrollmentReference(finger, autoExtract);

		if (useEncodedImage)
		{
			// So to add an encoded image first we generate one
			// This call converts it to WSQ format
			//ACDataBufferParameter imageEncoding;  
			byte [] imageEncoding = imageData.asEncodedImage(ImageFormat);


			fingerRef.addImage(imageEncoding, ImageFormat, ImageResolution);

		} else
		{
			// ALternatively just add the raw Image data, it will do the encoding
			// internally
			fingerRef.addImageData(imageData, ImageFormat);
		}

		if (!autoExtract)
		{
			// If autoextract is not used then we extract this in the normal way

			// Generate an instance of imageContext for image and finger
			ImageContext context = new ImageContext(imageData, finger);

			// Access the quality score - this is in score range 1 to 100
			QualityInfo qInfo = context.getQualityInfo();

			int qScore = qInfo.getQualityScore();

			ReferenceDataItem referenceData = context.getReferenceData();
			fingerRef.addReferenceData(referenceData);
			StructuredTemplate st = fingerRef.getStructuredTemplate();

			List<String> algorithms = st.getAlgorithms();
			for(String a1 :  algorithms)
			{
				printResult("algorithm", a1);
			}

		}

		// Quality score can also be accessed from the PK - this is in range 0 to 1
		int qualityScorePK = fingerRef.getQualityScore();
		printResult("Quality Score now in range 0 .. 100", qualityScorePK);
		return fingerRef;
	}
	
}
