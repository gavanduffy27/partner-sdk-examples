package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.Enums;
import com.genkey.abisclient.Enums.GKXmlSchema;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.QualityInfo;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.Subject;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.StringUtils;

public abstract class TransportExampleModule extends ExampleModule {

  static String ImageFormat = "wsq";

  static int ImageResolution = 500;

  /**
   * Generates the FingerEnrollmentReference for specified subject finger and sample index
   *
   * @param subjectId Subject identifier
   * @param finger Finger identifier
   * @param sampleIndex Sample index
   * @param useEncodedImage Use retrospective encoding of the image that is provided
   * @param autoExtract If true perform automatic extraction on receipt of the image.
   * @return
   */
  protected FingerEnrollmentReference generateFingerBlob(
      int subjectId, int finger, int sampleIndex, boolean useEncodedImage, boolean autoExtract) {
    // Access the image file
    String imageFile = TestDataManager.getImageFile(subjectId, finger, sampleIndex);

    // Create an instance of standard image data .
    ImageData imageData = TestDataManager.loadImage(imageFile);
    imageData.setResolution(ImageResolution);

    // Create the instance for specified finger.
    FingerEnrollmentReference fingerRef = new FingerEnrollmentReference(finger, autoExtract);

    if (useEncodedImage) {
      // So to add an encoded image first we generate one
      // This call converts it to WSQ format
      // ACDataBufferParameter imageEncoding;
      byte[] imageEncoding = imageData.asEncodedImage(ImageFormat);

      fingerRef.addImage(imageEncoding, ImageFormat, ImageResolution);

    } else {
      // ALternatively just add the raw Image data, it will do the encoding
      // internally
      fingerRef.addImageData(imageData, ImageFormat);
    }

    if (!autoExtract) {
      // If autoextract is not used then we extract this in the normal way

      // Generate an instance of imageContext for image and finger
      ImageContext context = new ImageContext(imageData, finger);

      // Access the quality score - this is in score range 1 to 100
      QualityInfo qInfo = context.getQualityInfo();

      int qScore = qInfo.getQualityScore();

      ReferenceDataItem referenceData = context.getReferenceData();
      fingerRef.addReferenceData(referenceData);
    }

    // Quality score can also be accessed from the PK - this is in range 0 to 1
    int qualityScorePK = fingerRef.getQualityScore();
    printResult("Quality Score now in range 0 .. 100", qualityScorePK);
    return fingerRef;
  }

  /**
   * Creates a new instance of a subject enrollment reference for specified subject, fingers and
   * samples. This constructs the instance by iterated calls to {@link #generateFingerBlob(int, int,
   * int, boolean, boolean)} which are then simply added to the container.
   *
   * @param subjectId
   * @param fingers
   * @param samples
   * @param useEncodedImage
   * @param autoExtract
   * @return
   */
  public SubjectEnrollmentReference generateSubjectBlob(
      int subjectId, int[] fingers, int[] samples, boolean useEncodedImage, boolean autoExtract) {
    // Instantiate the instance with the specified subjectId which is internally managed as string
    SubjectEnrollmentReference subjectReference =
        new SubjectEnrollmentReference(String.valueOf(subjectId));
    for (int finger : fingers) {
      for (int sample : samples) {
        FingerEnrollmentReference fingerReference =
            generateFingerBlob(subjectId, finger, sample, useEncodedImage, autoExtract);
        subjectReference.add(fingerReference);
      }
    }
    return subjectReference;
  }

  /**
   * Performs a simulated breadth first incremental enrollment where the enrollment data for a
   * subject is extended with an additional sample for the specified set of fingers.
   *
   * @param subjectReference
   * @param fingers
   * @param sampleIndex
   * @param useEncodedImage
   * @param autoExtract
   */
  public void enrollSubjectBlob(
      SubjectEnrollmentReference subjectReference,
      int[] fingers,
      int sampleIndex,
      boolean useEncodedImage,
      boolean autoExtract) {
    int subjectId = Integer.valueOf(subjectReference.getSubjectID());
    for (int finger : fingers) {
      FingerEnrollmentReference fingerReference =
          generateFingerBlob(subjectId, finger, sampleIndex, useEncodedImage, autoExtract);
      subjectReference.add(fingerReference);
    }
  }

  public void exportXMLFiles(String moduleName, SubjectEnrollmentReference subjectReference) {
    exportXMLFile(moduleName, subjectReference, Enums.GKXmlSchema.ABIS2);
    exportXMLFile(moduleName, subjectReference, Enums.GKXmlSchema.ABIS3);
  }

  public void exportXMLFile(
      String moduleName, SubjectEnrollmentReference subjectReference, GKXmlSchema schema) {

    // Construct the name of the test file
    String fileBaseName =
        StringUtils.concat(
            subjectReference.getSubjectID(),
            arrayIndex(subjectReference.getFingersPresent()),
            schema.toString());
    String testFile = super.getTestFile(moduleName, fileBaseName, "xml");

    // Access the XML representation of the subjectReference according to specified schema
    String xmlContent = subjectReference.toXml(schema);

    // Save to file.
    try {
      FileUtils.stringToFile(xmlContent, testFile);
    } catch (Exception e) {
      handleException(e);
    }
  }

  private int arrayIndex(int[] fingers) {
    return fingers.length * 10 + fingers[0];
  }

  public void subjectSerializeTest(SubjectEnrollmentReference sourceReference) {
    // Obtain binary encoding - suitable for any form of transmisson or hibernated persistence
    byte[] encoding = sourceReference.getEncoding();

    // Reconstruct the instance from the encoding
    SubjectEnrollmentReference copyRef = new SubjectEnrollmentReference(encoding);

    compareXML(sourceReference, copyRef);
  }

  public void compareXML(
      SubjectEnrollmentReference subjectReference1, SubjectEnrollmentReference subjectReference2) {
    String xml1 = subjectReference1.toXml(GKXmlSchema.ABIS3);
    String xml2 = subjectReference2.toXml(GKXmlSchema.ABIS3);
    if (xml1.equals(xml2)) {
      PrintMessage("Sucessful XML serialization readback test");
    } else {
      PrintMessage("Failure on XML serialization test");
    }
  }

  /// <summary>
  /// Extracts match subject from instance of SubjectEnrollmentReference
  /// </summary>
  /// <param name="subjectReference"></param>
  /// <returns></returns>
  Subject AsMatchSubject(SubjectEnrollmentReference subjectReference) {
    String subjectId = subjectReference.getSubjectID();
    Subject matchSubject = new Subject(subjectId);
    for (int finger : subjectReference.getFingersPresent()) {
      ReferenceDataItem reference = subjectReference.getConsolidatedReference(finger);
      matchSubject.addReference(reference);
    }
    return matchSubject;
  }
}
