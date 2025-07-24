package com.genkey.abisclient.examples.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.transport.StructuredTemplate;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.abisclient.verification.AnonymousFusionReference;
import com.genkey.abisclient.verification.AnonymousReferenceExtractor;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ImageUtils;
import com.genkey.platform.utils.Reference;
import com.genkey.platform.utils.ReflectUtils;
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

	private static final String EXT_SER = "ser";

	private static String ImageFormat = "bmp";
	
	public static String SingleFingerPath = "images/abisClientBMP/SingleFinger";
	public static String TwoFingerPath = "images/abisClientBMP/TwoFinger";
	public static String FourFingerPath = "images/abisClientBMP/FourFinger";
	public static String PortraitImagePath = "images/abisClientBMP/portrait";
	private static String ImageDirectory = SingleFingerPath;
	
	private static String ImageRootPath="images";
	
	private static String EnrollmentPath="partner/enrollments";
	
	
	private static boolean cacheEnabled=true;
	
	static boolean UseSubjectFolders=false;
	
	
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
	
	
	
	public static boolean isUseSubjectFolders() {
		return UseSubjectFolders;
	}

	public static void setUseSubjectFolders(boolean useSubjectFolders) {
		TestDataManager.UseSubjectFolders = useSubjectFolders;
	}



	public static int [] VerificationFingers = Commons.generateRangeV(3,4);
	
	
	 public static void setFingerImagePath(int nFingers)
     {
         String newPath = SingleFingerPath;
         switch(nFingers)
         {
             case 1:
                 newPath = SingleFingerPath;
                 break;
             case 2:
                 newPath = TwoFingerPath;
                 break;
             case 4:
                 newPath = FourFingerPath;
                 break;

             default:
                 newPath = getImageDirectory();
                 break;

         }
         setImageDirectory(newPath);
     }
	
	
	public static String makeFileName(long subjectId, int finger, int sampleIndex) {
		String baseName =  "S" + subjectId + "_F" + finger + "_" + sampleIndex;;
		if (TestDataManager.isUseSubjectFolders()) {
			baseName = FileUtils.mkFilePath2(subjectId, sampleIndex, baseName);
		}
		return baseName;
	}

	
	public static String getImageFile(String imageName) {
		return FileUtils.expandConfigFile(ImageDirectory, imageName, ImageFormat);
	}

	public static String getImageFile(String imageName,String path) {
		return FileUtils.expandConfigFile(path, imageName, ImageFormat);
	}
	
	/*
	public static String [] getImageFiles(long subjectId, int finger) {
		
	}
	*/
	
	public static String getImageFile(long subjectId, int finger, int sampleIndex) {
		String baseName =makeFileName(subjectId, finger, sampleIndex);
		return getImageFile(baseName);
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
	
	public static String [] getImageFiles(long subjectId) {
		String imageDirectory= TestDataManager.getImageDirectory();
		String pattern = TestFile.filePattern(subjectId, -1, -1);
		return FileUtils.getMatchingFilenames(imageDirectory, pattern, true, false);
	}
	
	public static List<Integer> getSubjectFingers(long subjectId, int sampleIndex) {
		String imageDirectory= TestDataManager.getImageDirectory();
		String [] fileNames;
		if (TestDataManager.isUseSubjectFolders()) {
			String imagePath = FileUtils.mkFilePath2(imageDirectory, subjectId, sampleIndex);
			fileNames = FileUtils.getFilenames(imagePath, true); 
		} else {
			fileNames = getImageFiles(subjectId);
		}
		List<Integer> result = TestFile.getFingers(fileNames);
		return result;
	}
	
	public static List<Long> getSubjects() {
		String imageDirectory= TestDataManager.getImageDirectory();
		List<Long> result = new ArrayList<>();
		if (TestDataManager.isUseSubjectFolders()) {
			String [] paths = FileUtils.getSubDirectories(imageDirectory);
			result = CollectionUtils.newList();
			for(String path : paths) {
				result.add(Long.valueOf(path));
			}
		} else {
			String [] fileNames = FileUtils.getFilenames(imageDirectory);
			result = TestFile.getSubjects(fileNames);
		}
		return result;
		
		
	}
	
	public static List<Integer> getSubjectSamples(long subjectId) {
		String imageDirectory= TestDataManager.getImageDirectory();
		List<Integer> result ;
		if (TestDataManager.isUseSubjectFolders()) {
			String imagePath = FileUtils.mkFilePath2(imageDirectory, subjectId);
			String [] paths = FileUtils.getSubDirectories(imagePath);
			result = CollectionUtils.newList();
			for(String path : paths) {
				result.add(Integer.valueOf(path));
			}
		} else {
			String [] fileNames = getImageFiles(subjectId);
			result = TestFile.getSamples(fileNames);
		}
		return result;
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
	
	
	public static ImageBlob enrolSubjectPortrait(int subject) {
		String fileName = getPortraitImageFile(subject);
		return loadPortraitImage(fileName);
	}
	
	private static ImageBlob loadPortraitImage(String fileName) {
		ImageBlob result;
		try {
			byte [] data = FileUtils.byteArrayFromFile(fileName);
			String format = FileUtils.extension(fileName);
			result = new ImageBlob(data, format);
		} catch (Exception e) {
			result=null;
		}
		return result;
	}

	public static String getPortraitImageFile(Object subject) {
		String baseName= String.format("face_%s", subject.toString());
		return FileUtils.expandConfigFile(PortraitImagePath, baseName, ImageUtils.EXT_JPEG);
	}

	public static ImageData loadImage(String fileName) {
		try {
			if (!FileUtils.isAbsolutePath(fileName)) {
				fileName = getImageFile(fileName);
			}
			boolean flg = FileUtils.existsFile(fileName);
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
		return loadReference(imageFile, finger, 0, false);
	}
	
	public static ReferenceDataItem loadReference(String imageFile, int finger, int version, boolean verificationMode) {
		ReferenceDataItem result = getCachedReference(imageFile, finger);
		if (result == null) {
			ImageData image = loadImage(imageFile);
			ImageContext context = new ImageContext(image, finger, version, verificationMode);
			result = context.getReferenceData();
			storeCachedReference(imageFile, finger, result);
		}
		return result;
	}
	
	public static List<ReferenceDataItem> loadVerificationReferences(int testSubject, int[] fingers, int sampleIndex,
			int version) {
		List<String> imageFiles = TestDataManager.getImageFiles(testSubject, fingers, sampleIndex);
		return loadReferences(imageFiles, fingers, version, true);
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
		return loadReferences(imageFiles, fingers, 0, false);
	}
	
	public static List<ReferenceDataItem> loadReferences(List<String> imageFiles, int [] fingers, int version, boolean verificationMode) {
		List<ReferenceDataItem> references = new ArrayList<ReferenceDataItem>();
		for(int ix=0; ix < imageFiles.size(); ix++) {
			String imageFile = imageFiles.get(ix);
			int fingerId = fingers[ix];
			ReferenceDataItem reference = loadReference(imageFile, fingerId, version, verificationMode);
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
		return FileUtils.expandConfigPath(ImageDirectory);
	}

	public static void setImageDirectory(String imageDirectory) {
		ImageDirectory = imageDirectory;
		Reference<Boolean> refFolders = Reference.create();
		Reference<String> refFormat = Reference.create();
		if (inferSubfolderPath(imageDirectory, refFolders)) {
			TestDataManager.UseSubjectFolders = refFolders.get();			
		}
		if (inferImageFormat(imageDirectory, refFormat)) {
			TestDataManager.ImageFormat = refFormat.get();
		}
	}
	
	
	
	
	public static String getImageRootPath() {
		return ImageRootPath;
	}

	public static void setImageRootPath(String imageRootPath) {
		ImageRootPath = FileUtils.expandConfigPath(imageRootPath);
	}

	public static void setImageSet(String setName) {
		String imagePath = FileUtils.expandConfigPath(getImageRootPath(), setName);
		TestDataManager.setImageDirectory(imagePath);
	}
	
	private static boolean inferImageFormat(String imageDirectory, Reference<String> refFormat) {
		boolean status=false;
		String [] files = null; 
		if (TestDataManager.isUseSubjectFolders()) {
			String [] subjects = FileUtils.getSubDirectories(imageDirectory, true);
			if (! CollectionUtils.isNullArray(subjects)) {
				String [] samples = FileUtils.getSubDirectories(subjects[0], true);
				if (!CollectionUtils.isNullArray(samples)) {
					files = FileUtils.getFilenames(samples[0]);
				}
			}
		} else {
			files = FileUtils.getFilenames(imageDirectory, false);
		}
		if (! CollectionUtils.isNullArray(files)) {
			if (FileUtils.hasExtension(files[0])) {
				String formatName = FileUtils.extension(files[0]);
				refFormat.set(formatName.toLowerCase());				
				status=true;
			}
		}
		return status;
	}

	
	private static boolean inferSubfolderPath(String imageDirectory, Reference<Boolean> isSubfolderPath) {
		String [] dirList = FileUtils.getSubDirectories(imageDirectory);
		String [] fileList = FileUtils.getFilenames(imageDirectory);
		boolean hasFiles = false;
		if (!CollectionUtils.isNullArray(fileList)) {
			for(String fileName : fileList) {
				if ( FileUtils.hasExtension(fileName)) {
					hasFiles=true;
					break;
				}
			}			
		}
		boolean hasFolders = (dirList != null && dirList.length > 0);
		boolean status = hasFolders != hasFiles;
		
		if(status) {
			isSubfolderPath.set(hasFolders);
		}
		
		return status;
	}

	public static void setEnrollmentPath(String enrollmentPath) {
		EnrollmentPath = enrollmentPath;
	}
	
	public static String getEnrollmentPath() {
		return FileUtils.expandConfigPath(EnrollmentPath);
	}
	
	
	public static String getEnrollmentFileName(Object subjectId, int sampleIndex) {
		String baseName = "EnrollmentRecord_" + subjectId + "_" + sampleIndex;
		return FileUtils.expandConfigFile(getEnrollmentPath(), baseName, EXT_SER);
	}
	
	public static boolean existsEnrollmentRecord(String subjectId, int sampleIndex) {
		String fileName = getEnrollmentFileName(subjectId, sampleIndex);
		return FileUtils.existsFile(fileName);
	}
	
	public static void saveEnrollmentRecord(Object subjectId, int sampleIndex, SubjectEnrollmentReference record) {
		byte [] encoding = record.getEncoding();
		String fileName = getEnrollmentFileName(subjectId, sampleIndex);
		try {
			FileUtils.byteArrayToFile(encoding, fileName, false);
		} catch (Exception e) {
			
		}
	}
	
	public static SubjectEnrollmentReference getEnrollmentRecord(Object subjectId, int sampleIndex) {
		SubjectEnrollmentReference result = null;
		String fileName = getEnrollmentFileName(subjectId, sampleIndex);
		if (FileUtils.existsFile(fileName)) {
			try {
				result = new SubjectEnrollmentReference();
				byte [] encoding = FileUtils.byteArrayFromFile(fileName);
				result.setEncoding(encoding);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return result;
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
		return createAnonymousReference(subject, useLegacy, VerificationFingers, 1);
	}
	
	public static AnonymousFusionReference  createAnonymousReference(long subject, boolean useLegacy, int [] fingers, int sampleIndex) {
		List<String> fileNames = getImageFiles(subject, VerificationFingers, sampleIndex);
		int version = useLegacy ? ABISClientLibrary.getLegacyVersion() :  ABISClientLibrary.getLatestVersion();
		//ImageContextSDK.setLegacyVerificationMode(useLegacy);
		List<ReferenceDataItem> references = TestDataManager.loadReferences(fileNames, fingers, version, false);
		if (references.size() > 0) {
			ReferenceDataItem refItem = references.get(0);
			StructuredTemplate sTemplate = new StructuredTemplate(refItem);
			//sTemplate.getVersion();
			//FormatUtils.printObject("Reference", references.get(0).printReferenceState());
		}
		AnonymousReferenceExtractor extractor = new AnonymousReferenceExtractor();
		AnonymousFusionReference  result = extractor.createFromReferences(references, null);
		//ImageContextSDK.setDefaultVerificationMode();
		return result;
	}
	
	
	public static String getSubjectKeyFile(long subjectId, boolean isLegacy) {
		String rootPath = FileUtils.expandConfigPath("templatesJ/biohash");
		int version = ABISClientLibrary.getDefaultVersion(isLegacy);
		String pathExt = "version_" + version;
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

	public static String resolveFileName(String fileName) {
        if (FileUtils.isAbsolutePath(fileName))
        {
            return fileName;
        }

        List<String> fileNames = new ArrayList<String>();
        List<String> paths = new ArrayList<String>();

        if (FileUtils.hasExtension(fileName))
        {
            fileNames.add(fileName);
        } 
        else 
        {
            String file1 = FileUtils.applyExtension(fileName, "bmp");
            String file2 = FileUtils.applyExtension(fileName, "pgm");
            fileNames.add(file1);
            fileNames.add(file2);
        }

        String localImagePath = FileUtils.mkFilePath(FileUtils.CURRENT_DIR, "images");
        String globalImagePath = FileUtils.mkFilePath(FileUtils.getConfigurationPath(), "images");
        paths.add(FileUtils.CurrentPath);
        paths.add(localImagePath);
        paths.add(FileUtils.getConfigurationPath());
        paths.add(globalImagePath);
        paths.add(TestDataManager.getImageDirectory());

        String testFile = "";

        Reference<String> testFileRef = Reference.create();
        if (existsAnyFile(paths, fileNames, testFileRef)) 
        {
            testFile = testFileRef.get();
        }

        return testFile;
	}

	private static boolean existsAnyFile(List<String> paths, List<String> fileNames, Reference<String> testFile)
    {
		boolean status=false;
        for(String path : paths)
        {
            for(String fileName : fileNames)
            {
            	if (status) {
            		break;
            	}
                String filePath = FileUtils.mkFilePath(path, fileName);
                if (FileUtils.existsFile(filePath))
                {
                    testFile.set(filePath);
                    status=true;
                    break;
                }
            }
        }
        return status;
    }
	
	
	public static class BookMark {
		String imageFormat;
        String imagePath;
        boolean useSubjectFolders=false;

        public BookMark()
        {
            this.imageFormat = TestDataManager.getImageFormat();
            this.imagePath = TestDataManager.getImageDirectory();
            this.useSubjectFolders = TestDataManager.isUseSubjectFolders();
        }

        public void Restore()
        {
            TestDataManager.setImageDirectory(this.imagePath);
            TestDataManager.setImageFormat(this.imageFormat);
            TestDataManager.setUseSubjectFolders(this.useSubjectFolders);
        }
		
	}
	
	public static class TestFile {
		long subject=-1;
		int finger=-1;
		int sample=-1;
		
		
		public TestFile() {
			
		}

		public static List<Long> getSubjects(String[] fileNames) {
			List<Long> result = CollectionUtils.newList();
			for(String fileName : fileNames) {
				long sample =new TestFile(fileName).subject;
				if (sample > 0) {
					result.add(sample);
				}
			}
			return result;
		}

		public static List<Integer> getSamples(String[] fileNames) {
			List<Integer> result = CollectionUtils.newList();
			for(String fileName : fileNames) {
				int sample =new TestFile(fileName).sample;
				if (sample > 0) {
					result.add(sample);
				}
			}
			return result;
		}

		public static List<Integer> getFingers(String[] fileNames) {
			List<Integer> result = CollectionUtils.newList();
			for(String fileName : fileNames) {
				int finger =new TestFile(fileName).finger;
				if (finger > 0) {
					result.add(finger);
				}
			}
			Collections.sort(result);
			return result;
		}

		public TestFile(long subject) {
			this();
			this.subject=subject;
		}

		public TestFile(long subject, int finger) {
			this(subject);
			this.finger=finger;
		}

		public TestFile(long subject, int finger, int sample) {
			this(subject, finger);
			this.sample=sample;
		}
		
		
		public TestFile(String name) {
			fromFileName(name);
		}
		
		public void fromFileName(String fileName) {
			String baseName = FileUtils.baseName(fileName);
			List<String> tokens = Commons.getRecordTokens(baseName, "_");
			try {
				this.subject = Long.valueOf(tokens.get(0).substring(1));
				this.finger = Integer.valueOf(tokens.get(1).substring(1));
				this.sample = Integer.valueOf(tokens.get(2));
			} catch (Exception e) {
				
			}
		}
		
		public String toString() {
			return baseName(subject, finger, sample);
		}
		
		public String filePattern() {
			return filePattern(subject, finger, sample);
		}
		
		public static String filePattern(long subject, int finger, int sampleIndex) {
			return "^" + baseName(subject, finger, sampleIndex) + ".*";
		}
				
		public static String baseName(long subject, int finger, int sampleIndex) {
			String stem = baseName(subject, finger);
			return sampleIndex > 0 ? stem + "_" + sampleIndex : stem;
		}
	
		public static String baseName(long subject, int finger) {
			String stem = baseName(subject);
			return finger < 1 ? stem : stem + "_F" + finger;
		}
	
		public static String baseName(long subject) {
			return "S" + subject;
		}
	
	}


}
