package com.genkey.abisclient.examples;

//import java.awt.image.BufferedImage;
import java.util.List;

import com.genkey.abisclient.ImageData;

/**
 * Functionally complete implementation of UserMessageHandler with specializing class reqquiring only to 
 * implement a minimal set of methods for display of text and image.
 * 
 * @author Gavan
 *
 */
public abstract class AbstractUserMessageHandler implements UserMessageHandler{

	@Override
	public void showImageList(String title, List<ImageData> imageList, int nrows, int nColumns, double scaleFactor) {
		//BufferedImage bigImage = ImageUtils.createImageGrid(imageList, 0, 0, scaleFactor);
		//showImageFeedback(bigImage, title);
	}

	@Override
	public void printHeader(String text) {
		printHeader(text, '=');
	}

	@Override
	public void printHeader(String text, char ch) {
		printMessage("");
		String headerLine = makeString(ch, text.length());
		printMessage(text);
		printMessage(headerLine);
	}

	private static String makeString(char ch, int nChars) {
		char chars[] = new char[nChars];
		for(int ix = 0 ; ix < nChars ; ix++) {
			chars[ix] = ch;
		}
		return new String(chars);
	}

	
	
	
}
