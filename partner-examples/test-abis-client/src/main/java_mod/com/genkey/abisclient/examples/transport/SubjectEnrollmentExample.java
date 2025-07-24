package com.genkey.abisclient.examples.transport;

import java.util.List;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.platform.utils.Commons;

public class SubjectEnrollmentExample extends TransportExampleModule {

	@Override
	protected void runAllExamples() {
		doExample();
	}
	
    // Just constructs a Subject blob which can be passed fro front end to back end as binary encoding (rather than XML document
    // Back end can also read this from Java
    public void doExample()
    {
        // Perform test on subect 1
        int[] fingers = Commons.generateRangeV(3, 4, 7, 8);
        runSampleTest(1, fingers);
    }

    public void runSampleTest(int subjectId, int [] fingers)
    {
        SubjectEnrollmentReference subjectRef = generateEnrollmentSubject(subjectId, fingers, 1);
        transportTest(subjectRef);
    }

    public  SubjectEnrollmentReference generateEnrollmentSubject(int subjectId, int[] fingers, int sampleIndex)
    {
        SubjectEnrollmentReference subjectRef = new SubjectEnrollmentReference(String.valueOf(subjectId));
        String name = subjectRef.getSubjectID();

        for(int finger : fingers) {
        	printMessage("Entolling finger " + finger);
            FingerEnrollmentReference fingerRef = generateFingerBlob(subjectId, finger, 1, true, false);
            subjectRef.add(fingerRef);
        }
        return subjectRef;
    }


    public void transportTest(SubjectEnrollmentReference xmitRef)
    {
        int size = xmitRef.getEncodingSize();

        byte[] encoding = xmitRef.getEncoding();

        // intervening transport

        // Consumer client - typically ABIS integration socket
        SubjectEnrollmentReference recvRef = new SubjectEnrollmentReference(encoding);
        byte[] encoding2 = recvRef.getEncoding();

        int[] fingers = recvRef.getFingersPresent();
        for(int finger : fingers) {
        	printMessage("Processing finger " + finger);
            FingerEnrollmentReference fingerRef = recvRef.getReferenceForFinger(finger);

            Boolean hasImage = fingerRef.hasImage();

            if (hasImage)
            {
                int imageResolution = fingerRef.getImageResolution();
                printResult("Resolution", imageResolution);
                ImageBlob blob = fingerRef.getImageBlob();
                String format = blob.getImageFormat();
                byte[] imageData = blob.getImageEncoding();
            }
            else
            {
                ImageBlob blob = fingerRef.getImageBlob();
                int resolution = fingerRef.getImageResolution();
                String format = fingerRef.getImageFormat();
            }

            Boolean hasReference = fingerRef.hasReference();

            if (hasReference)
            {
                // Finger reference can now be deconstructed as in FingerEnrollmentRefence example
                StructuredTemplate structureRef = fingerRef.getStructuredTemplate();


                List<String> algorithms = structureRef.getAlgorithms();

                for(String algorithm : algorithms)
                {
                    byte[] templateEncoding = structureRef.getAlgorithmEncoding(algorithm);
                    byte[] abis3Encoding = structureRef.getABIS3TemplateEncoding(algorithm);
                    String xmlTemplateName = StructuredTemplate.asABIS3TemplateName(algorithm);
                    // This can now be added to the XML document but we need to take care with the algorithm-nams
                    // to use the name that ABIS is expecting
                    printResult(xmlTemplateName, abis3Encoding.length);
                }
            }
        }




    }
	

}
