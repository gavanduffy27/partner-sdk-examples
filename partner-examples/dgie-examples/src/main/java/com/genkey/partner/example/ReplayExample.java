package com.genkey.partner.example;

import java.io.IOException;

import org.junit.Test;

import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.platform.utils.FileUtils;
import com.genkey.platform.utils.FormatUtils;

public class ReplayExample extends PartnerExample{
	
	public static String REPLAY_PATH="dgie/replay";

	public static String LOCAL_PATH="./replay";
	
	public static void main(String [] args) {
		FormatUtils.printBanner("Modified test");
		ReplayExample test = new ReplayExample();
		//test.runTestCase4();
		//test.runTestExamples();
		test.processCommandLine(args);
	}	
	
	
	public String getEncodingReplayPath() {
		String path = FileUtils.expandConfigPath(REPLAY_PATH);
		if (FileUtils.isDirectory(LOCAL_PATH)) {
			path = LOCAL_PATH;
		}
		return path;
	}
	
	@Test
	public void replayEncodingExample() {
		String path = getEncodingReplayPath();
		if (! FileUtils.isDirectory(path)) {
			printError("Replay path " + path + " does not exist");
			return;
		}
		String [] fileNames = FileUtils.getFilenames(path, "bin", true);
		for(String fileName : fileNames) {
			replayEncodingExample(fileName);
		}
	}

	public void replayEncodingExample(String fileName) {

		try {
			byte [] encoding = FileUtils.byteArrayFromFile(fileName);
			replayEncodingExample(encoding);
		} catch (IOException e) {
			
		}
		
	}

	private void replayEncodingExample(byte[] encoding) {
		// TODO Auto-generated method stub
		SubjectEnrollmentReference reference = new SubjectEnrollmentReference();
		reference.setEncoding(encoding);
		testInsert(reference);
	}

	public void testInsert(SubjectEnrollmentReference reference) {
		GenkeyABISService service = this.getABISService();
		MatchEngineResponse response = service.insertSubject(reference, false);
		if (!response.isSuccess()) {
			String message = response.getErrorMessage();
			printError(message);
		} else {
			printResult(response.getOperationResult());
			if (	response.hasMatchResults() ) {		
				int index=0;
				for (MatchResult result : response.getMatchResults()) {
					super.printIndexResult("Score", index++, result.getMatchScore());
				}
			} 
			
		}
	}
}
