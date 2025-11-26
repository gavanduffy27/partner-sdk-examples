package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import java.util.List;

public class SubjectEnrollmentExample extends TransportExampleModule {

  @Override
  protected void runAllExamples() {
    doBinaryExample();
    doXmlExample();
    doMultiSampleExample();
  }

  public void doXmlExample() {
    SubjectEnrollmentReference subjectReference =
        super.generateSubjectBlob(1, FourFingers, SingleSample, false, true);
    super.exportXMLFiles("singleSample", subjectReference);
    super.subjectSerializeTest(subjectReference);
  }

  public void doMultiSampleExample() {
    SubjectEnrollmentReference subjectReference =
        super.generateSubjectBlob(1, FourFingers, TwoSamples, false, true);
    exportXMLFiles("multiSample", subjectReference);
    subjectSerializeTest(subjectReference);
  }

  // Just constructs a Subject blob which can be passed fro front end to back end as binary encoding
  // (rather than XML document
  // Back end can also read this from Java
  public void doBinaryExample() {
    runSampleTest(1, FourFingers);
  }

  public void runSampleTest(int subjectId, int[] fingers) {
    SubjectEnrollmentReference subjectRef = generateEnrollmentSubject(subjectId, fingers, 1);
    transportTest(subjectRef);
  }

  public SubjectEnrollmentReference generateEnrollmentSubject(
      int subjectId, int[] fingers, int sampleIndex) {
    SubjectEnrollmentReference subjectRef =
        new SubjectEnrollmentReference(String.valueOf(subjectId));
    String name = subjectRef.getSubjectID();

    for (int finger : fingers) {
      printMessage("Entolling finger " + finger);
      FingerEnrollmentReference fingerRef = generateFingerBlob(subjectId, finger, 1, true, false);
      subjectRef.add(fingerRef);
    }
    return subjectRef;
  }

  public void transportTest(SubjectEnrollmentReference xmitRef) {
    int size = xmitRef.getEncodingSize();

    byte[] encoding = xmitRef.getEncoding();

    // intervening transport

    // Consumer client - typically ABIS integration socket
    SubjectEnrollmentReference recvRef = new SubjectEnrollmentReference(encoding);
    byte[] encoding2 = recvRef.getEncoding();

    int[] fingers = recvRef.getFingersPresent();
    for (int finger : fingers) {
      printMessage("Processing finger " + finger);

      // Access all samples for given finger
      List<FingerEnrollmentReference> fingerRefs = recvRef.getReferencesForFinger(finger);

      // Alternative iteration based on query of sample count and sample specific access

      int sampleCount = recvRef.getFingerSampleCount(finger);

      for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
        printMessage("Processing finger " + finger + " using sample " + sampleIndex);

        FingerEnrollmentReference fingerRef =
            recvRef.getReferenceForFingerSample(finger, sampleIndex);

        // Query if image is present
        Boolean hasImage = fingerRef.hasImage();

        if (hasImage) {

          int imageResolution = fingerRef.getImageResolution();
          printResult("Resolution", imageResolution);

          // Access format and encoding directly
          ImageBlob blob = fingerRef.getImageBlob();
          String format = blob.getImageFormat();
          byte[] imageEncoding = blob.getImageEncoding();

          // Access the image as the SDK Image object
          ImageData sdkImage = fingerRef.getImageData();
        }

        // Query if reference templates are present
        Boolean hasReference = fingerRef.hasReference();

        if (hasReference) {
          // Finger reference can now be deconstructed as in FingerEnrollmentRefence example
          StructuredTemplate structureRef = fingerRef.getStructuredTemplate();

          List<String> algorithms = structureRef.getAlgorithms();

          for (String algorithm : algorithms) {
            // Obtain encoding as used by ABIS 2 architecture components
            byte[] templateEncoding = structureRef.getAlgorithmEncoding(algorithm);

            // Obtain the raw binary encoding that is compliant with the core vendor library
            byte[] rawEncoding = structureRef.getRawEncoding(algorithm);

            // Obtain the ABIS3 algorithm encoding which for recent versions is the same as raw
            // encoding
            byte[] abis3Encoding = structureRef.getABIS3TemplateEncoding(algorithm);

            String xmlTemplateName = StructuredTemplate.asABIS3TemplateName(algorithm);
            // This can now be added to the XML document but we need to take care with the
            // algorithm-nams
            // to use the name that ABIS is expecting
            printResult(xmlTemplateName, abis3Encoding.length);
          }
        }
      }
    }
  }
}
