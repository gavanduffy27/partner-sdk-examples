package com.genkey.partner.workshop;

import java.io.IOException;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.platform.utils.FileUtils;

public class ImageBlobExample extends BMSWorkshopExample {
  public static void main(String[] args) {
    ImageBlobExample test = new ImageBlobExample();
    test.processCommandLine(args);
  }

  @Override
  protected void runAllExamples() {
	testAutoCorrectFace();	  
    testJP2Correction();
  }
  
  public void testAutoCorrectFace() {
	    String jp2File = "./test/portrait.jp2";;
	    SubjectEnrollmentReference enrollReference = new SubjectEnrollmentReference();
	    SubjectEnrollmentReference enrollReference2 = new SubjectEnrollmentReference();

	    try {
	      byte[] encoding = FileUtils.byteArrayFromFile(jp2File);

	      ImageBlob imageBlob = new ImageBlob(encoding, ImageData.FORMAT_JPEG);
	      
	      // Will fix on assignment
	      enrollReference.setFacePortrait(imageBlob);

	      enrollReference2.setFacePortrait(imageBlob);
	      
	      boolean status = enrollReference.getFacePortrait().validateFormat();
	      if (!status) {
	    	  printError("Bad status on face portrait");
	      }
	      
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	  
  }

  public void testJP2Correction() {
    String jp2File = "./test/portrait.jp2";;
    SubjectEnrollmentReference reference = new SubjectEnrollmentReference();

    try {
      byte[] encoding = FileUtils.byteArrayFromFile(jp2File);

      ImageBlob imageBlob = new ImageBlob(encoding, ImageData.FORMAT_JPEG);
      boolean status = imageBlob.validateFormat();
      if (!status) {
        imageBlob.enforceFormat(ImageData.FORMAT_JPEG);
      }
      
      status = imageBlob.validateFormat();
      
      status = imageBlob.enforceFormat(ImageData.FORMAT_JPEG);

      byte[] imageEncoding = imageBlob.getImageEncoding();
      String jpegFile = FileUtils.forceExtension(jp2File, ImageData.FORMAT_JPEG);
      FileUtils.byteArrayToFile(imageEncoding, jpegFile);

    } catch (Exception e) {
    	e.printStackTrace();
    }
  }
}
