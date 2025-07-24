package com.genkey.abisclient.examples.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;

/**
 * Various file utilities used to support the test examples. Implementation of these is of no importance
 * and they are provided for purpose of expedience only.
 * <p>
 * Worth noting however that this implements the use of global configuration directories using the exact
 * same mechanism as the product libraries, where the root configuration location on disk is assumed to
 * be ${GENKEY_HOME}\config where GENKEY_HOME is defined as a environment variable.
 * @author gavan
 *
 */
public class FileUtils {

	static String configurationPath;
	static String ENV_IST_CONFIG = "IST_CONFIG_HOME";
	static String ENV_GENKEY_HOME = "GENKEY_HOME";

	private static final String DEFAULT_CONFIG_PATH = "./";
	
	public final static String FILE_SEP = ".";
	public final static String DIR_SEP = "/";
	public static final String CURRENT_DIR = ".";
	public static final String PARENT_DIR = "..";
	public static final String CurrentPath = CURRENT_DIR;
	
	public static class FileNameStruct {
		String dirName;
		String baseName;
		String extension;
		
		public String getFileName() {
			return FileUtils.mkFilePath(dirName, getShortName());
		}
		
		public String getShortName() {
			return FileUtils.mkFileName(baseName, extension);
		}
		
		public String toString() {
			return getFileName();
		}
	}

	public static String mkFileName(String fileName, String ext) {
		return fileName + FILE_SEP + ext;
	}
	
	public static String mkFilePath(String dirName, String fileName, boolean checkPath) throws IOException{
		if (checkPath) {;
			FileUtils.checkPathExists(dirName);
		}
		return dirName + DIR_SEP + fileName;
	}
	
	public static String mkFilePath(String dirName, String fileName) {
		return (dirName == null ? fileName : dirName + DIR_SEP + fileName);
	}

	public static String mkFilePath2(Object ... components) {
		boolean flgFirst=true;
		StringBuffer result = new StringBuffer();
		for(Object component : components) {
			if (flgFirst) {
				result.append(component.toString());
				flgFirst=false;
			} else {
				result.append(DIR_SEP).append(component);
			}
		}
		return result.toString();
	}
	
	public static String expandExtension(String fileName, String ext) {
		if (FileUtils.hasExtension(fileName)) {
			return fileName;
		} else {
			return mkFileName(fileName, ext);
		}
	}

	public static boolean hasExtension(String fileName) {
		String shortName = FileUtils.shortName(fileName);
		return shortName.indexOf(FILE_SEP) > 1;
	}

	public static String forceExtension(String fileName, String ext) {
		FileNameStruct struct = FileUtils.parseFileName(fileName);;
		struct.extension = ext;
		return struct.getFileName();
	}
	
	public static String extendBaseName(String fileName, String suffix) {
		FileNameStruct fileInfo = parseFileName(fileName);
		fileInfo.baseName += "_" + suffix;
		return fileInfo.getFileName();
	}
	
	/**
	 * checks the specified pathName exists and creates it if not
	 * 
	 * @throws java.io.IOException
	 */
	public static void checkPathExists(String pathName) throws IOException {
		// Debug.print("checking file-path for " + pathName);
		if (!existsFile(pathName)) {
			createDirectory(pathName, true);
		}
	}

	public static void createDirectory(String fileName, boolean tryCreatePath)
			throws IOException {
		if (existsFile(fileName)) {
			return;
		}

		String pathName = dirName(fileName);
		if (tryCreatePath) {
			checkPathExists(pathName);
		}
		File file = new File(fileName);
		file.mkdir();
	}
	
	/** returns true if specified file exists */
	public static boolean existsFile(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}
	
	public static boolean isDirectory(String pathName) {
		File file = new File(pathName);
		return file.exists() && file.isDirectory();
	}
	
	public static FileNameStruct parseFileName(String fileName) {
		FileNameStruct struct = new FileNameStruct();
		struct.dirName = FileUtils.dirName(fileName);
		String shortName = FileUtils.shortName(fileName);
		struct.baseName = baseName(shortName);
		struct.extension = FileUtils.extension(shortName);
		return struct;
	}
	
	public static boolean isAbsolutePath(String fileName) {
		return ((fileName.indexOf("/") == 0) || (fileName.indexOf(":") == 1) || fileName.indexOf(".") == 0);
	}

	public static String shortName(String pathName) {
		String shortName = pathName;
		int pos = getDirectorySplit(pathName);
		if (pos > 0) {
			shortName = pathName.substring(pos + 1);
		}
		return shortName;
	}

	public static String dirName(String pathName) {
		String shortName = null;
		int pos = getDirectorySplit(pathName);
		if (pos > 0) {
			shortName = pathName.substring(0, pos);
		}
		return shortName;
	}

	public static String baseName(String fileName) {
		String shortName = shortName(fileName);
		String baseName = shortName;
		int pos = shortName.lastIndexOf(FILE_SEP);
		if (pos > 0) {
			baseName = shortName.substring(0, pos);
		}
		return baseName;
	}

	public static String extension(String fileName) {
		String shortName = shortName(fileName);
		String ext = "";
		int pos = shortName.lastIndexOf(FILE_SEP);
		if (pos > 0) {
			ext = shortName.substring(pos+1);
		}
		return ext;
	}
	
	public static int getDirectorySplit(String pathName) {
		int pos1 = pathName.lastIndexOf(DIR_SEP);
		int pos2 = pathName.lastIndexOf('\\');
		return Math.max(pos1, pos2);

	}

	public static String expandConfigFile(String pathName, String fileName, String ext) {
		try{
			String fullPath = expandConfigPath(pathName);
			checkPathExists(fullPath);
			return FileUtils.expandFileName(fullPath, fileName, ext);

		} catch (IOException e) {
			throw new RuntimeException("Failed to establish configuration path " + pathName, e);
		}
	}

	public static String expandConfigPath(String pathName) {
		return FileUtils.expandFilePath(getConfigurationPath(), pathName);
	}
	
	public static String expandConfigPath(String pathName, String subPath) {
		return FileUtils.expandFilePath(getConfigurationPath(), FileUtils.mkFilePath(pathName, subPath));
	}

	public static String getConfigurationPath() {
		if (configurationPath == null) {
			setDefaultConfigurationPath();
		}
		return configurationPath;
	}

	private static void setDefaultConfigurationPath() {
		if (configurationPath == null) {
			String path = System.getenv(ENV_IST_CONFIG);
			configurationPath = path; 
		}
		if (configurationPath == null) {
			String path = System.getenv(ENV_GENKEY_HOME);
			String cfgPath = FileUtils.mkFilePath(path, "config");
			configurationPath = cfgPath;
			
		}
		if (configurationPath == null) {
			configurationPath = DEFAULT_CONFIG_PATH;
		}
		
	}
	
	public static void setConfigurationPath(String dirName) {
		configurationPath = dirName;
	}

	public static void setConfigurationPathEnv(String envVar) {
		String path = System.getenv(envVar);
		if (path != null ) {
			setConfigurationPath(path);
		}
	}
	
	
	
	public static String expandFileName(String pathName, String fileName,
			String ext) {
		String fullName = applyExtension(fileName, ext);

		if (!isAbsolutePath(fileName)) {
			fullName = mkFilePath(pathName, fullName);
		}

		return fullName;
	}

	public static String applyExtension(String fileName, String ext) {
		if (ext == null) {
			return fileName;
		}
		String extension = FileUtils.extension(fileName);
		if (extension != null && !isNullString(extension)) {
			return fileName;
		}
		
		return FileUtils.mkFileName(fileName, ext);
		
	}

	public static boolean isNullString(String extension) {
		return extension == null || extension.trim().length() == 0;
	}

	public static String expandFilePath(String rootName, String pathName) {
		String fullPath = pathName;
		if (!isAbsolutePath(pathName)) {
			fullPath = mkFilePath(rootName, pathName);
		}

		return fullPath;
	}
	
	/**
	 * loads contents of a file into a byte array
	 * 
	 * @throws java.io.IOException
	 */
	public static byte[] byteArrayFromFile(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(new File(fileName));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		copyStream(fis, bos);
		return bos.toByteArray();
	}
	
	public static void byteArrayToFile(byte[] data, String fileName) throws IOException {
		byteArrayToFile(data, fileName, false);
	}
	
	public static void byteArrayToFile(byte[] data, String fileName, boolean append)
			throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		FileOutputStream fos = new FileOutputStream(fileName, append);
		copyStream(bis, fos);
		fos.flush();
		fos.close();
	}
	
	/** Writes specified string to file 
	 * @throws IOException */
	public static void stringToFile(String text, String fileName) throws IOException {
		stringToFile(text, fileName, false);
	}
	
	
	/**
	 * Writes specified string to file optionally appending to existing contents
	 * @param text			String to be written to file
	 * @param fileName		Target file name
	 * @param append		If true then append otherwise overwrite
	 * @throws IOException
	 */
	public static void stringToFile(String text, String fileName, boolean append)
			throws IOException {
		byte [] data = Commons.stringToByteArray(text);
		FileUtils.byteArrayToFile(data, fileName, append);
	}

	public static String stringFromFile(String fileName) throws IOException {
		byte [] data = byteArrayFromFile(fileName);
		return Commons.byteArrayToString(data);
	}

	public static byte[] byteArrayFromBase64File(String fileName) throws IOException {
		String b64Encoding = stringFromFile(fileName);
		return Commons.BASE64Decode(b64Encoding);
	}
	
	public static void byteArrayToBase64File(byte [] data, String fileName) throws Exception {
		String b64Encoding = Commons.BASE64Encode(data);
		stringToFile(b64Encoding, fileName);
	}

	public static void copyStream(InputStream is, OutputStream os)
			throws IOException {
		int bufferSize = 1024;
		int nBytes = 0;
		byte[] buffer = new byte[bufferSize];
		while ((nBytes = is.read(buffer)) > 0) {
			os.write(buffer, 0, nBytes);
		}
		is.close();
		os.flush();
		os.close();
	}

	public static String[] getFilenames(String dirName) {
		return getFilenames(dirName, false);
	}
	
	public static String[] getFilenames(String dirName, boolean expandName) {
		return getFilenames(dirName, null, expandName);
	}

	/**
	 * returns a list of files in specified directory that match the specified
	 * extension
	 * 
	 * @param dirName
	 *            The directory in which to look
	 * @param ext
	 *            Extension to filter by - null retrieves all files
	 */
	public static String[] getFilenames(String dirName, String ext, boolean expandName) {
		FilenameFilter filter = new ExtensionFileFilter(ext);
		File file = new File(dirName);
		String [] fileNames = file.list(filter);
		if (expandName) {
			FileUtils.updatePath(dirName, fileNames);
		}
		return fileNames;
	}
	
	public static String[] getMatchingFilenames(String dirName, String pattern, boolean expandName) {
		return getMatchingFilenames(dirName, pattern, expandName, false);
	}
	
	public static String[] getMatchingFilenames(String dirName, String pattern, boolean expandName, boolean caseInsensitive) {
		FilenameFilter filter = new NameMatchFilter(pattern, caseInsensitive);
		File file = new File(dirName);
		String [] fileNames = file.list(filter);
		if (expandName) {
			FileUtils.updatePath(dirName, fileNames);
		}
		return fileNames;
	}
	
	
	public static void updatePath(String dirName, String [] fileNames) {
		if (fileNames == null) {
			return;
		}
		for(int ix = 0 ; ix < fileNames.length; ix++) {
			fileNames[ix] = FileUtils.mkFilePath(dirName, fileNames[ix]);
		}
	}
	
	/**
	 * returns a list of sub-directories in specified directory
	 * 
	 * @param dirName
	 *            The directory in which to look
	 */
	public static String[] getSubDirectories(String dirName) {
		FilenameFilter filter = new DirectoryFileFilter();
		File file = new File(dirName);
		String [] result = file.list(filter);
		if (!CollectionUtils.isNullArray(result)) {
			Arrays.sort(result);
		}
		return result != null ? result : new String [0];
	}

	/**
	 * Returns list of subdirectories optionally updating the path
	 * @param dirName
	 * @param updatePath
	 * @return
	 */
	public static String[] getSubDirectories(String dirName, boolean updatePath) {
		String [ ]results = getSubDirectories(dirName);
		if (updatePath) {
			FileUtils.updatePath(dirName, results);
		}
		return results;
	}
	
	
	
	
}


/** Filters file-names by extension */
class ExtensionFileFilter implements FilenameFilter {

	String m_ext = null;
	

	public ExtensionFileFilter(String ext) {
		m_ext = ext;
	}

	public boolean accept(File file, String fileName) {
		String fullName = FileUtils.mkFilePath(file.getPath(), fileName);
		File thisFile = new File(fullName);
		return thisFile.isFile() && checkExtension(fileName);
	}

	private boolean checkExtension(String fileName) {
		boolean status;
		if (m_ext == null) {
			status = true;
		} else {
			String ext = getExtension(fileName);
			status = ext.equals(m_ext);
		}
		return status;
	}

	/** retrieves the file extension for specified file-name */
	public String getExtension(String fileName) {
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos + 1);
	}
	

}



/** Filters file-names to include directories only */
class DirectoryFileFilter implements FilenameFilter {

	public DirectoryFileFilter() {
	}

	public boolean accept(File file, String fileName) {
		String fullName = FileUtils.mkFilePath(file.getPath(), fileName);
		File thisFile = new File(fullName);
		return thisFile.isDirectory();
	}

}

class PlainFileFilter implements FilenameFilter {

	public PlainFileFilter() {
	}

	public boolean accept(File file, String fileName) {
		String fullName = FileUtils.mkFilePath(file.getPath(), fileName);
		File thisFile = new File(fullName);
		return thisFile.isFile();
	}

}


class NameMatchFilter implements FilenameFilter {

	String pattern;
	boolean caseInsensitive=false;

	NameMatchFilter(String pattern) {
		this(pattern, false);
	}

	NameMatchFilter(String pattern, boolean caseInsensitive) {
		setPattern(pattern);
		this.caseInsensitive=caseInsensitive;
	}
	
	
	public boolean accept(File dir, String name) {
		if (caseInsensitive) {
			name = name.toLowerCase();
		}
		return name.matches(getPattern());
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}