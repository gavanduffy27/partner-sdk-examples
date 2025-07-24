package com.genkey.abisclient.examples.utils;

import java.awt.image.BufferedImage;

import com.genkey.abisclient.ImageData;
import com.genkey.platform.utils.ImageUtils;

public class ABISClientUtils {
	
	public static BufferedImage asBufferedImage(ImageData imageData) {
		BufferedImage image = ImageUtils.createBufferedImage(imageData.getWidth(), imageData.getHeight(), 
					imageData.getPixelData());
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
	public static void fromBufferedImage(ImageData imageData, BufferedImage bufferedImage, int targetResolution) {
		byte [] pixelData = ImageUtils.getImageData(bufferedImage);
		int wd = bufferedImage.getWidth();
		int ht = bufferedImage.getHeight();
		imageData.setWidth(wd);
		imageData.setHeight(ht);
		imageData.setPixelData(pixelData);
		imageData.setResolution(targetResolution);
	}

	public static void rescaleResolution(ImageData imageData, int targetResolution) {
		//imageData.rescaleImage(targetResolution);
	}


}
