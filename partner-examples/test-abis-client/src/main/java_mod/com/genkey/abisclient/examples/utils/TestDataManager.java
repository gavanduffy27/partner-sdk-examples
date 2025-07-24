package com.genkey.abisclient.examples.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceExtractor;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.StringUtils;

/**
 * Utilities for expedient generation of test data for code examples.
 * This is based on accessing image test files that are typically provided within the GENKEY_HOME
 * configuration area.
 * <p>
 * Note there are routines for easy loading of biometric ReferenceDataItems from test images which
 * are not intended to be indicative of how this would be achieved in a live capture context.
 * @author gavan
 *
 */
public class TestDataManager {

	private static String ImageFormat = "pgm";
	private static String ImageDirectory = "images/abisClientExample";
	
	private static boolean cacheEnabled=true;
	
	
	static String [] CurrentFamilies = {
	"fastafis7",
	"fastafis7.common",
	"biofinger"	
	};

	static String [] LegacyFamilies = {
	"flexkey",
	"fastafis4.common",
	"fastafis4",
	"isokit"	
	};

	public static List<String> getActiveFamilies(boolean active) {
		boolean useLegacy = ExampleModule.isUseLegacy();
		return active ? getLegacyFamilies(useLegacy) : getLegacyFamilies(! useLegacy);
	}

	public static  List<String> getLegacyFamilies(boolean useLegacy) {
		return useLegacy ? asList(LegacyFamilies) : asList(CurrentFamilies);
	}

	private static List<String> asList(String [] args) {
		return Commons.arrayToList(args);
	}
	
	
	
	public static int [] VerificationFingers = Commons.generateRangeV(3,4);
	
	public static String makeFileName(long subjectId, int finger, int sampleIndex) {
		return  "S" + subjectId + "_F" + finger + "_" + sampleIndex;
	}
	
	public static String getImageFile(String imageName) {
		return FileUtils.expandConfigFile(ImageDirectory, imageName, ImageFormat);
	}
	
	public static String getImageFile(long subjectId, int finger, int sampleIndex) {
		return getImageFile(makeFileName(subjectId, finger, sampleIndex));
	}
	
	public static List<String> getImageFiles(long subjectId, int [] fingers, int sampleIndex) {
		List<String> imageFiles = new ArrayList<String>();
		for(int finger : fingers) {
			String imageFile = getImageFile(subjectId, finger, sampleIndex);
			imageFiles.add(imageFile);
		}
		return imageFiles;
	}

	public static List<String> getImageFiles(long subjectId, int finger, int [] samples) {
		List<String> imageFiles = new ArrayList<String>();
		for(int sample : samples) {
			String imageFile = getImageFile(subjectId, finger, sample);
			imageFiles.add(imageFile);
		}
		return imageFiles;
	}
	
	public static ImageData loadImage(long subjectId, int finger, int sampleIndex) {
		String fileName = getImageFile(subjectId, finger, sampleIndex);
		return loadImage(fileName);
	}

	public static List<ImageData> loadImages(int subject, int finger, int[] samples) {
		List<String> fileNames = getImageFiles(subject, finger, samples);
		return loadImages(fileNames);
	}

	public static List<ImageData> loadImages(int subject, int []fingers, int sample) {
		List<String> fileNames = getImageFiles(subject, fingers, sample);
		return loadImages(fileNames);
	}
	
	
	public static ImageData loadImage(String fileName) {
		try {
			if (!FileUtils.isAbsolutePath(fileName)) {
				fileName = getImageFile(fileName);
			}
			String format = FileUtils.extension(fileName).toLowerCase();
			byte [] encoding = FileUtils.byteArrayFromFile(fileName);
			return new ImageData(encoding, format);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<ImageData> loadImages(List<String> imageFiles) {
		List<ImageData> images = new ArrayList<ImageData>();
		for(String imageFile : imageFiles) {
			ImageData image = loadImage(imageFile);
			images.add(image);
		}
		return images;
	}

	static Map<String, ReferenceDataItem> referenceCache = CollectionUtils.newMap();
	
	static String getCacheKey(String imageFile, int finger) {
		return StringUtils.concat(imageFile, finger);
	}
	
	static ReferenceDataItem getCachedReference(String imageFile, int finger) {
		if (!cacheEnabled) {
			return null;
		}
		String key = getCacheKey(imageFile, finger);
		return referenceCache.get(key);
	}
	
	public static void setCacheEnabled(boolean setting) {
		cacheEnabled=setting;
	}
	
	static void storeCachedReference(String imageFile, int finger, ReferenceDataItem reference) {
		if (!cacheEnabled) {
			return;
		}
		if (reference != null ) {
			String key = getCacheKey(imageFile, finger);
			referenceCache.put(key, reference);
		}
	}
	
	public static ReferenceDataItem loadReference(String imageFile, int finger) {
		ReferenceDataItem result = getCachedReference(imageFile, finger);
		if (result == null) {
			ImageData image = loadImage(imageFile);
			ImageContext context = new ImageContext(image, finger);
			result = context.getReferenceData();
			storeCachedReference(imageFile, finger, result);
		}
		return result;
	}
	
	public static List<ReferenceDataItem> loadReferences(long subject, int [] fingers, int sampleIndex) {
		List<String> imageFiles = TestDataManager.getImageFiles(subject, fingers, sampleIndex);
		return loadReferences(imageFiles, fingers);
	}

	public static List<ReferenceDataItem> loadReferences(long subject, int finger, int [] sampleIndices) {
		List<String> imageFiles = TestDataManager.getImageFiles(subject, finger, sampleIndices);
		return loadReferences(imageFiles, finger);
	}
	
	public static List<ReferenceDataItem> loadReferences(List<String> imageFiles, int finger) {
		int [] fingers = new int[imageFiles.size()];
		for(int ix=0; ix < fingers.length; ix++) {
			fingers[ix]=finger;
		}
		return loadReferences(imageFiles, fingers);
	}
	
	public static List<ReferenceDataItem> loadReferences(List<String> imageFiles, int [] fingers) {
		List<ReferenceDataItem> references = new ArrayList<ReferenceDataItem>();
		for(int ix=0; ix < imageFiles.size(); ix++) {
			String imageFile = imageFiles.get(ix);
			int fingerId = fingers[ix];
			ReferenceDataItem reference = loadReference(imageFile, fingerId);
			references.add(reference);
		}
		return references;
	}

	public static int[] generateArray(int start, int nValues) {
		return generateArray(start, nValues, 1);
	}

	private static int[] generateArray(int start, int nValues, int step) {
		int [] result = new int[nValues];
		for(int ix=0; ix < nValues; ix++) {
			result[ix] = start + ix * step;
		}
		return result;
	}

	public static String getImageFormat() {
		return ImageFormat;
	}

	public static void setImageFormat(String imageFormat) {
		ImageFormat = imageFormat;
	}

	public static String getImageDirectory() {
		return ImageDirectory;
	}

	public static void setImageDirectory(String imageDirectory) {
		ImageDirectory = imageDirectory;
	}

	public static AnonymousFusionReference getAnonymousReference(long subject, boolean useLegacy, boolean flgCreate) {
		try {
			String fileName = getSubjectKeyFile(subject, useLegacy);
			boolean existsFile = FileUtils.existsFile(fileName);
			AnonymousFusionReference result = null;
			if ( existsFile) {
				byte [] encoding = FileUtils.byteArrayFromBase64File(fileName);
				result = new AnonymousFusionReference(encoding);
			} else if (flgCreate) {
				result = createAnonymousReference(subject, useLegacy);
				cacheAnonymousReference(subject, useLegacy, result);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void cacheAnonymousReference(long subject, boolean useLegacy, AnonymousFusionReference fusionReference) {
		String fileName = getSubjectKeyFile(subject, useLegacy);
		try {
			FileUtils.byteArrayToBase64File(fusionReference.getData(), fileName); 
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
	public static AnonymousFusionReference  createAnonymousReference(long subject, boolean useLegacy) {
		return createAnonymousReference(subject, useLegacy, VerificationFingers, 0);
	}
	
	public static AnonymousFusionReference  createAnonymousReference(long subject, boolean useLegacy, int [] fingers, int sampleIndex) {
		List<String> fileNames = getImageFiles(subject, VerificationFingers, 1);
		ImageContextSDK.setLegacyVerificationMode(useLegacy);
		List<ReferenceDataItem> references = TestDataManager.loadReferences(fileNames, fingers);
		AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();
		AnonymousFusionReference  result = extractor.createFromReferences(references, null);
		ImageContextSDK.setDefaultVerificationMode();
		return result;
	}
	
	
	public static String getSubjectKeyFile(long subjectId, boolean isLegacy) {
		String rootPath = FileUtils.expandConfigPath("templatesJ/biohash");
		String pathExt = isLegacy ? "legacy" : "current";
		String filepath = FileUtils.mkFilePath(rootPath, pathExt);
		String baseName = "biohash_" + subjectId;
		return FileUtils.expandConfigFile(filepath, baseName, "b64");
	}

	public static List<String> getImageFileNames() {
		String imagePath = FileUtils.expandConfigPath(getImageDirectory());
		String [] fileNames = FileUtils.getFilenames(imagePath, getImageFormat(), false);
		List<String> result = new ArrayList<>();
		for(String fileName : fileNames) {
			String baseName = FileUtils.baseName(fileName);
			result.add(baseName);
		}
		return result;
	}

	
}
