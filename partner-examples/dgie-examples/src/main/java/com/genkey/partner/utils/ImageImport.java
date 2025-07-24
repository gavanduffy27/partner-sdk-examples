package com.genkey.partner.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.genkey.abisclient.ABISClientLibrary;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.example.PartnerTestSuite;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.ListBatchChunker;

/**
 * Provides utilities for test data generation
 * @author Gavan
 *
 */
public class ImageImport {

	
	static final String PATH_SFINGE_RAW="partner/images/set1_raw";
	static final String PATH_SFINGE_IMPORT="partner/images/set_100";
	
	
	public static void main(String [] args) {
//		PartnerTestSuite.init();
		ABISClientLibrary.initializeDefault();
//		importSfingeData(1000, 2);
		createEnrollmentRecords();
		
	}
	
	
	public static void createEnrollmentRecords() {
		String domainName = PartnerExample.getPartnerDomainName();
		String targetDirectory = FileUtils.expandConfigPath(PATH_SFINGE_IMPORT);
		TestDataManager.setImageDirectory(targetDirectory);
//		TestDataManager.setImageFormat(ImageData.FORMAT_WSQ);
		boolean hasfolders = TestDataManager.isUseSubjectFolders();
		String imageFormat = TestDataManager.getImageFormat();
		List<Long> subjects = TestDataManager.getSubjects();
		for(long subject : subjects) {
			EnrollmentUtils.accessEnrollmentRecords(subject, domainName);
		}
		
	}
	
	/**
	 * Imports data from a raw SFINGE source path
	 */
	public static void importSfingeData(int startId, int nSamples) {
		String sourceDirectory = FileUtils.expandConfigPath(PATH_SFINGE_RAW);
		String targetDirectory = FileUtils.expandConfigPath(PATH_SFINGE_IMPORT);
		
		TestDataManager.setImageDirectory(targetDirectory);
	
		TestDataManager.setUseSubjectFolders(true);
			
		SFingerFileComparator comparator = new SFingerFileComparator();
		
		String [] files = FileUtils.getFilenames(sourceDirectory, ImageData.FORMAT_WSQ, true);
		
		// If we sort then it will respect sample order
		List<String> fileNames = Commons.arrayToList(files);
		Collections.sort(fileNames, comparator);
		ListBatchChunker<String> chunker = new ListBatchChunker<>(fileNames, 10 * nSamples);
		int subjectId=startId;
		while(chunker.hasMore()) {
			List<String> batch = chunker.getNextBatch();
			importSfingeSubject(subjectId++, batch, nSamples);
		}
	}


	private static void importSfingeSubject(int subjectId, List<String> batch, int nSamples) {
		int nFingers = batch.size();
		ListBatchChunker<String> chunker = new ListBatchChunker<>(batch, nSamples);		
		int finger=1;
		while(chunker.hasMore()) {
			List<String> sampleFiles = chunker.getNextBatch();
			importSfingeSubject(subjectId, finger++, sampleFiles);
		}
	}


	private static void importSfingeSubject(int subjectId, int finger, List<String> sampleFiles) {
		int sampleIndex=1;
		for(String fileName : sampleFiles) {
			checkFormat(fileName);
			String targetFile = TestDataManager.getImageFile(subjectId, finger, sampleIndex++);
			
			try {
				FileUtils.copyFile(fileName, targetFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private static void checkFormat(String fileName) {
		String format = FileUtils.extension(fileName);
		if (! TestDataManager.getImageFormat().equalsIgnoreCase(format)) {
			TestDataManager.setImageFormat(format);
		}
	}
	
	
	
	
}


class SFingerFileComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		SfingeExportFile f1 = new SfingeExportFile(o1);
		SfingeExportFile f2 = new SfingeExportFile(o2);
		return f1.compareTo(f2);
	}
	
}

class SfingeExportFile implements Comparable<SfingeExportFile> {
	
	String fileName;
	
	int index=-1;
	
	String stem="";
	
	int sampleIndex=-1;
	
	String format=null;
	
	public SfingeExportFile(String fileName) {
		fromFileName(fileName);
	}

	void fromFileName(String fileName) {
		String baseName = FileUtils.baseName(fileName);
		this.format = FileUtils.extension(fileName);
		List<String> tokens = Commons.getTokens(baseName, "_");
		try {
			stem = tokens.get(0);
			index = Integer.valueOf(tokens.get(1));
			sampleIndex = Integer.valueOf(tokens.get(2));
		} catch (Exception e) {
			
		}
	}

	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	public int getSampleIndex() {
		return sampleIndex;
	}

	public void setSampleIndex(int sampleIndex) {
		this.sampleIndex = sampleIndex;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public int compareTo(SfingeExportFile that) {
		int diff = this.getStem().compareTo(that.stem);
		if (diff == 0) {
			diff = new Integer(this.index).compareTo(that.index);
		}
		
		if (diff == 0) {
			diff = new Integer(this.sampleIndex).compareTo(that.sampleIndex);
		}
		
		return diff;
	}

	
}
