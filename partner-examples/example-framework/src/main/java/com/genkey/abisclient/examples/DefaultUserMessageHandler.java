package com.genkey.abisclient.examples;

import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.ScannerDialogs;
import com.genkey.platform.utils.FormatUtils;

public class DefaultUserMessageHandler extends AbstractUserMessageHandler {

  boolean useGui = false;

  public DefaultUserMessageHandler() {}

  public DefaultUserMessageHandler(boolean useGui) {
    this.setUseGui(useGui);
  }

  public boolean isUseGui() {
    // return useGui;
    return false;
  }

  public void setUseGui(boolean useGui) {
    // this.useGui = useGui;
  }

  @Override
  public void printMessage(String message) {
    FormatUtils.println(message);
  }

  @Override
  public boolean promptContinue(String message) {
    return promptOperatorContinue(message, this.isUseGui());
  }

  @Override
  public void showImageFeedback(ImageData image, String title) {}

  protected static boolean promptOperatorContinue(String errorDescription, boolean useGui) {
    String message = "Prompt:\n" + errorDescription;
    /*
    if (useGui) {
    	return SwingDialogs.promptContinue(message, "Operator prompt");
    } else {
    	return ScannerDialogs.promptContinue(message, "Operator prompt");
    }
    */
    return ScannerDialogs.promptContinue(message, "Operator prompt");
  }
}
