package com.genkey.fingerprint.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

	public static BufferedImage asBufferedImage(ImageData imageData) throws IOException {
		byte[] bmpEncoding = imageData.asEncodedImage(CaptureUtils.FORMAT_BMP);
		ByteArrayInputStream stream = new ByteArrayInputStream(bmpEncoding);
		return ImageIO.read(stream);
	}

	public static ImageData imageDataFromBuffered(BufferedImage image) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(image, CaptureUtils.FORMAT_BMP, stream);
		byte[] imageEncoding = stream.toByteArray();
		return new ImageData(imageEncoding, CaptureUtils.FORMAT_BMP);
	}

	public static BufferedImage rotateImage(BufferedImage srcImage, double radians) {
		if (srcImage == null) {
			return null;
		}
		BufferedImage result;
		try {
			AffineTransform txf = AffineTransform.getRotateInstance(radians, srcImage.getWidth() / 2,
					srcImage.getHeight() / 2);
			AffineTransformOp op = new AffineTransformOp(txf, null);
			result = op.filter(srcImage, null);
		} catch (Exception e) {
			e.printStackTrace();
			result = srcImage;
		}
		return result;
	}

}
