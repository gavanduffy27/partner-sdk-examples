package com.genkey.abisclient.examples.utils;

import com.genkey.abisclient.ImageData;
import com.genkey.platform.utils.ImageUtils;
import java.awt.image.BufferedImage;
import org.slf4j.LoggerFactory;

public class ABISClientUtils {

  static org.slf4j.Logger logger = LoggerFactory.getLogger(ABISClientUtils.class);

  public static BufferedImage asBufferedImage(ImageData imageData) {
    BufferedImage image =
        ImageUtils.createBufferedImage(
            imageData.getWidth(), imageData.getHeight(), imageData.getPixelData());
    return image;
  }

  public static void fromBufferedImage(ImageData imageData, BufferedImage bufferedImage) {
    fromBufferedImage(imageData, bufferedImage, 500);
  }

  /**
   * Modifies imageData with new image contents and resolution.
   *
   * @param imageData
   * @param bufferedImage
   * @param targetResolution
   */
  public static void fromBufferedImage(
      ImageData imageData, BufferedImage bufferedImage, int targetResolution) {
    byte[] pixelData = ImageUtils.getImageData(bufferedImage);
    int wd = bufferedImage.getWidth();
    int ht = bufferedImage.getHeight();
    imageData.setWidth(wd);
    imageData.setHeight(ht);
    imageData.setPixelData(pixelData);
    imageData.setResolution(targetResolution);
  }

  public static void rescaleResolution(ImageData imageData, int targetResolution) {
    imageData.rescaleImage(targetResolution);
  }

  public static void convertImages(String directory, String sourceFormat, String tgtFormat) {
    convertImages(directory, sourceFormat, tgtFormat, 500);
  }

  public static void convertImages(
      String directory, String sourceFormat, String tgtFormat, int sourceResolution) {
    String[] files = FileUtils.getFilenames(directory, sourceFormat, true);
    for (String file : files) {
      String tgtFile = FileUtils.forceExtension(file, tgtFormat);
      if (!FileUtils.existsFile(tgtFile)) {
        try {
          BufferedImage image = ImageUtils.bufferedImageFromFile(tgtFile);
          ImageData imageData = new ImageData();
          fromBufferedImage(imageData, image);
          imageData.setResolution(sourceResolution);
          if (sourceResolution != 500) {
            ABISClientUtils.rescaleResolution(imageData, 500);
          }
          byte[] encoding = imageData.asEncodedImage(tgtFormat);
          if (sourceResolution != 500) {
            tgtFile = FileUtils.extendBaseName(tgtFile, String.valueOf(sourceResolution));
          }
          FileUtils.byteArrayToFile(encoding, tgtFile, false);
        } catch (Exception e) {
          logger.error("Unexpected failure in file transform of " + file);
        }
      }
    }
  }
}
