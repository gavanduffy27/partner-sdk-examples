package com.genkey.abisclient.examples;

import com.genkey.abisclient.ImageData;
import java.util.List;

/**
 * Note almost all functions are provided in the Abstract base handler implementation,
 *
 * @author Gavan
 */
public interface UserMessageHandler {
  /**
   * Prints message feedback to user
   *
   * @param message
   */
  void printMessage(String message);

  boolean promptContinue(String message);

  void showImageFeedback(ImageData image, String title);

  void showImageList(
      String title, List<ImageData> imageList, int nrows, int nColumns, double scaleFactor);

  void printHeader(String text);

  void printHeader(String text, char ch);
}
