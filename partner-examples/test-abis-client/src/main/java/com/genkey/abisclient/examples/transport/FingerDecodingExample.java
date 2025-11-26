package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.SemlexEncodeUtils;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.platform.utils.EncodingUtils;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FingerDecodingExample extends TransportExampleModule {

  @Override
  protected void runAllExamples() {
    testDecodeXavier();
    testSemlexEncoding();
    testJavaEncoding();
  }

  public void testSemlexEncoding() {
    FingerEnrollmentReference fingerBlob = generateFingerBlob(1, 3, 1, true, false);
    byte[] standardEncoding = fingerBlob.getEncoding();

    int fingerId = fingerBlob.getFingerID();
    byte[] referenceData = fingerBlob.getReferenceDataItem().getReferenceData();
    byte[] imageEncoding = fingerBlob.getImageEncoding();

    try {
      byte[] semlexEncoding =
          SemlexEncodeUtils.encodeSemlexFormat(fingerId, referenceData, imageEncoding, "wsq");

      FingerEnrollmentReference fingerBlob2 = javaDecodeFingerReference(semlexEncoding);
      FingerEnrollmentReference fingerBlob3 = new FingerEnrollmentReference(semlexEncoding);

      checkBlobsEqual(fingerBlob, fingerBlob2);
      assertEqual(semlexEncoding, standardEncoding);

    } catch (Exception e) {

    }
  }

  // static String tetsPath= FileUtils.expandConfigPath("test/semlex/simba/xavier");
  public void testDecodeXavier() {
    String testPath = FileUtils.expandConfigPath("test/semlex/simba/xavier");
    String[] fileNames = FileUtils.getFilenames(testPath, "PK", true);
    for (String fileName : fileNames) {
      try {
        String baseName = FileUtils.baseName(fileName);
        byte[] data = FileUtils.byteArrayFromFile(fileName);
        FingerEnrollmentReference fingerBlob = javaDecodeFingerReference(data);
        int finger = fingerBlob.getFingerID();
        showImage(fingerBlob.getImageEncoding(), ImageData.FORMAT_WSQ, baseName);
        showStructure(fingerBlob.getReferenceDataItem());
      } catch (Exception e) {
        handleTestException(e);
      }
    }
  }

  public void testJavaEncoding() {
    FingerEnrollmentReference fingerBlob = generateFingerBlob(1, 3, 1, true, false);

    showImage(fingerBlob.getImageData(), ImageData.FORMAT_WSQ, "standard");
    showStructure(fingerBlob.getReferenceDataItem());

    byte[] encoding = fingerBlob.getEncoding();
    FingerEnrollmentReference fingerBlob2 = javaDecodeFingerReference(encoding);
    byte[] encoding2 = fingerBlob2.getEncoding();

    checkBlobsEqual(fingerBlob, fingerBlob2);

    assertEqual(encoding, encoding2);

    ImageData imageData = fingerBlob.getImageData();
    byte[] encodingImage2 = imageData.asEncodedImage(ImageFormat);
    byte[] imageEncoding = fingerBlob.getImageEncoding();

    ReferenceDataItem dataItem = fingerBlob.getReferenceDataItem();
    byte[] encoding3 = javaEncodeFingerReference(dataItem, imageEncoding, ImageData.FORMAT_WSQ);
    FingerEnrollmentReference fingerBlob3 = javaDecodeFingerReference(encoding3);
    checkBlobsEqual(fingerBlob, fingerBlob3);
    assertEqual(encoding, encoding3);
  }

  private void checkBlobsEqual(
      FingerEnrollmentReference fingerBlob, FingerEnrollmentReference fingerBlob2) {
    byte[] image1 = fingerBlob.getImageEncoding();
    byte[] image2 = fingerBlob2.getImageEncoding();
    printResult("Image1", image1.length);
    printResult("Image2", image2.length);
    byte[] refData1 = fingerBlob.getReferenceDataItem().getReferenceData();
    byte[] refData2 = fingerBlob2.getReferenceDataItem().getReferenceData();
    this.showImage(image1, ImageFormat, "original");
    this.showImage(image2, ImageFormat, "regenerate");
    assertEqual(refData1, refData2);
    assertEqual(image1, image2);
  }

  public FingerEnrollmentReference javaDecodeFingerReference(byte[] encoding) {
    // ByteArrayInputStream bis = new ByteArrayInputStream(encoding);
    DataInputStream dis = asInputStream(encoding);
    FingerEnrollmentReference result;
    try {
      long sid1 = EncodingUtils.decodeInt64(dis, false);
      int fingerId = dis.readByte();
      result = new FingerEnrollmentReference(fingerId, false);
      boolean hasImage = asBoolean(dis.readByte());
      if (hasImage) {
        int resolution = EncodingUtils.decodeInt16(dis, false);
        String format = EncodingUtils.decodeString(dis, false);
        byte[] imageData = EncodingUtils.decodeByteArray(dis, false);
        ImageData image = new ImageData(imageData, format);
        showImage(imageData, format, "decode");
        image.setResolution(resolution);
        result.addImage(imageData, format, resolution);
      }
      boolean hasReference = asBoolean(dis.readByte());
      if (hasReference) {
        byte[] data = readRestStream(dis);
        ReferenceDataItem refData = new ReferenceDataItem(data);
        showStructure(refData);

        result.addReferenceData(refData);
        ReferenceDataItem refData2 = result.getReferenceDataItem();
        byte[] refEncoding2 = refData2.getReferenceData();
        assertEqual(data, refEncoding2);
        int fId = result.getFingerID();
      }
      int score = result.getQualityScore();
    } catch (Exception e) {
      result = null;
      handleException(e);
    }
    return result;
  }

  public byte[] javaEncodeFingerReference(
      ReferenceDataItem dataItem, byte[] imageEncoding, String format) {
    byte[] refData = dataItem.getReferenceData();
    FormatUtils.printResult("RefData", refData.length);
    FormatUtils.printResult("ImageEncoding", imageEncoding.length);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    byte[] result = null;
    try {
      EncodingUtils.encodeLong(dos, 420, false);
      dos.writeByte(dataItem.getFingerID());
      dos.write(1);
      EncodingUtils.encodeInt16(dos, 500, false);
      EncodingUtils.encodeString(dos, format, false);
      EncodingUtils.encodeByteArray(dos, imageEncoding, false);

      dos.writeByte(1);
      dos.write(dataItem.getReferenceData());
      dos.close();
      result = bos.toByteArray();
      bos.close();
    } catch (Exception e) {
      handleException(e);
    }
    return result;
  }

  private void showImage(ImageData image, String format, String context) {
    byte[] imageData = image.asEncodedImage(format);
    printResult("ImageData", imageData.length);
    showImage(imageData, format, context);
  }

  private void showImage(byte[] imageData, String format, String context) {
    try {
      BufferedImage image = ImageUtils.bufferedImageFromData(imageData, format);
      String imageFile = getTestFile(context, format);
      ImageUtils.bufferedImageToFile(image, imageFile);
      // ImageUtils.showImage(image);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public byte[] readRestStream(InputStream stream) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    FileUtils.copyStream(stream, bos);
    return bos.toByteArray();
  }

  private static void showStructure(ReferenceDataItem refData) {
    try {
      byte[] refEncoding = refData.getReferenceData();
      inspectReferenceEncoding(refEncoding);
      FormatUtils.printResult("RefTemplate", refEncoding.length);
      StructuredTemplate structTemplate = new StructuredTemplate(refData);
      List<String> algorithms = structTemplate.getAlgorithms();
      for (String algorithm : algorithms) {
        byte[] encoding = structTemplate.getABIS3TemplateEncoding(algorithm);
        FormatUtils.printResult(algorithm, encoding.length);
      }

    } catch (Exception e) {

    }
  }

  private static void inspectReferenceEncoding(byte[] refEncoding) {
    try {
      DataInputStream dis = asInputStream(refEncoding);
      long sid = EncodingUtils.decodeInt64(dis, false);
      int pid = EncodingUtils.decodeInt16(dis, false);
      boolean compacted = asBoolean(dis.readByte());

      int nComponents = EncodingUtils.decodeInt16(dis, false);
      for (int ix = 0; ix < 1; ix++) {
        byte modality = dis.readByte();
        byte algType = dis.readByte();
        String name = EncodingUtils.decodeString(dis, false);
        FormatUtils.printResult("Algorithm", name);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static boolean asBoolean(byte value) {
    return value == 0 ? false : true;
  }

  static DataInputStream asInputStream(byte[] encoding) {
    return new DataInputStream(new ByteArrayInputStream(encoding));
  }
}
