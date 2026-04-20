package com.genkey.partner.utils;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.genkey.abisclient.ImageData;

public class ImageUtils {
  public static ImageData rotateImageData(ImageData imageData, double theta) {
    try {
      BufferedImage image = asBufferedImage(imageData);
      BufferedImage rotateImage = rotateImage(image, theta);
      return imageDataFromBuffered(rotateImage);
    } catch (Exception e) {
      throw new RuntimeException("Unhandled failure on image rotate", e);
    }
  }

  public static ImageData rotateImage180(ImageData imageData) {
    int wd = imageData.getWidth();
    int ht = imageData.getHeight();
    byte[] srcData = imageData.getPixelData();
    byte[] newImage = new byte[imageData.getWidth() * imageData.getHeight()];
    for (int row = 0; row < ht; row++) {
      int tgtRow = ht - row - 1;
      for (int col = 0; col < wd; col++) {
        int tgtCol = wd - col - 1;
        int srcIndex = getPixelIndex(row, col, wd, ht);
        int tgtIndex = getPixelIndex(tgtRow, tgtCol, wd, ht);
        newImage[tgtIndex] = srcData[srcIndex];
      }
    }
    return new ImageData(wd, ht, newImage, imageData.getResolution());
  }

  public static int getPixelIndex(int row, int col, int wd, int ht) {
    return row * wd + col;
  }

  public static BufferedImage asBufferedImage(ImageData imageData) {
    return createBufferedImage(
        imageData.getWidth(), imageData.getHeight(), imageData.getPixelData());
  }

  public static BufferedImage createBufferedImage(int wd, int ht, byte[] pixels) {
    BufferedImage result = new BufferedImage(wd, ht, BufferedImage.TYPE_BYTE_GRAY);
    final byte[] resultPixels = ((DataBufferByte) result.getRaster().getDataBuffer()).getData();
    //		final byte[] pixels = getImageData();
    int imageSize = wd * ht;
    System.arraycopy(pixels, 0, resultPixels, 0, imageSize);
    return result;
  }

  public static ImageData imageDataFromBuffered(BufferedImage image) throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ImageIO.write(image, ImageData.FORMAT_BMP, stream);
    byte[] imageEncoding = stream.toByteArray();
    return new ImageData(imageEncoding, ImageData.FORMAT_BMP);
  }

  public static BufferedImage rotateImage(BufferedImage srcImage, double radians) {
    if (srcImage == null) {
      return null;
    }
    BufferedImage result;
    try {
      AffineTransform txf =
          AffineTransform.getRotateInstance(
              radians, srcImage.getWidth() / 2, srcImage.getHeight() / 2);
      AffineTransformOp op = new AffineTransformOp(txf, null);
      result = op.filter(srcImage, null);
    } catch (Exception e) {
      e.printStackTrace();
      result = srcImage;
    }
    return result;
  }

  public static boolean checkEqual(ImageData image1, ImageData image2) {
    return image1.getHeight() == image2.getHeight()
        && image1.getWidth() == image2.getWidth()
        && Arrays.equals(image1.getPixelData(), image2.getPixelData());
  }
}
