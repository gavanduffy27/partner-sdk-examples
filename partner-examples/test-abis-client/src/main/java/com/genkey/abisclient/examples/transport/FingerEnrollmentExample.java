package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.Minutia;
import com.genkey.abisclient.MinutiaTemplate;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;
import java.util.List;

public class FingerEnrollmentExample extends TransportExampleModule {

  @Override
  protected void runAllExamples() {
    doFingerEnrollmentExample();
  }

  void doFingerEnrollmentExample() {
    int[] fingers = Commons.generateRangeV(3, 4, 7, 8);
    doFingerEnrollmentExample(1, fingers, true, false);
    doFingerEnrollmentExample(2, fingers, true, true);
    doFingerEnrollmentExample(3, fingers, false, true);
    doFingerEnrollmentExample(4, fingers, false, false);
  }

  private void doFingerEnrollmentExample(
      int subjectId, int[] fingers, boolean useEncodedImage, boolean autoExtract) {
    for (int finger : fingers) {
      FingerEnrollmentReference fingerBlob =
          generateFingerBlob(subjectId, finger, 1, useEncodedImage, autoExtract);

      // First tun with compact= false
      transportTestFingerBlob(fingerBlob, true);
    }
  }

  private void transportTestFingerBlob(
      FingerEnrollmentReference xmitReference, boolean useCompact) {

    // Access the encoding
    byte[] encoding = xmitReference.getEncoding();

    // Send to far off party and later in any language binding of the SDK
    // For this example we assume a .NET receiver

    // Reconstitute instance from encoding
    FingerEnrollmentReference recvReference = new FingerEnrollmentReference(encoding);

    // ? which finger
    int fingerId = recvReference.getFingerID();
    printResult("Finger", fingerId);

    if (recvReference.hasImage()) {

      // It would be unusual not to ave image but it is supported through SubjectEnrollmentReference
      // to dispose images

      // Resolution
      int imageResolution = recvReference.getImageResolution();
      ImageBlob rawImage = recvReference.getImageBlob();

      // Alternatively access the raw data
      ImageData imageData = recvReference.getImageData();
    }

    if (recvReference.hasReference()) {
      // Access standard opaque reference
      ReferenceDataItem referenceData = recvReference.getReferenceDataItem();

      // Cant do much with that so access structure template
      StructuredTemplate compoundRef = recvReference.getStructuredTemplate();

      // Has no effect for now, but we should call it.
      if (useCompact) {
        compoundRef.compactReference();
      }

      // So what does it have
      List<String> algorithms = compoundRef.getAlgorithms();

      // Access the raw encoding
      for (String algorithm : algorithms) {
        byte[] rawTemplate = compoundRef.getAlgorithmEncoding(algorithm);
        // Process as we want - add to XML ?
        byte[] abis3Encoding = compoundRef.getABIS3TemplateEncoding(algorithm);

        // Can now access correct name from XML - at least this is the name used within binary
        // encoding
        // Assume this is also correct for XML
        String abis3TemplateName = StructuredTemplate.asABIS3TemplateName(algorithm);
        printMessage(
            "Processing ABIS3 template "
                + abis3TemplateName
                + " of size "
                + abis3Encoding.length
                + " from raw encoding of size "
                + rawTemplate.length);
      }

      MinutiaTemplate minutiaTemplate = compoundRef.getMinutiaTemplate();
      int minutiaCount = minutiaTemplate.getMinutiaCount();

      int index = 0;
      for (Minutia feature : minutiaTemplate.getMinutiaVector()) {
        // Process minutia
        printIndexResult("minutia", index++, feature);
      }
    }

    if (recvReference.hasImage() && recvReference.hasReference()) {
      byte[] bmpImage = recvReference.drawMinutiaTemplate();
      String testFile = super.getTestFile("minutiaTemplate", ImageData.FORMAT_BMP);
      try {
        FileUtils.byteArrayToFile(bmpImage, testFile);
      } catch (Exception e) {
        handleException(e);
      }
    }

    if (recvReference.hasImage()) {
      byte[] encoding1 = recvReference.getEncoding();
      recvReference.disposeImage();
      byte[] encoding2 = recvReference.getEncoding();
      FormatUtils.printResult(
          "Encoding reduction before/after", encoding1.length + "/" + encoding2.length);
    }
  }
}
