package com.genkey.abisclient.examples.utils;

import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import com.genkey.platform.utils.FormatUtils;


public class ScannerDialogs {
	
	public static void main(String [] args) {
		boolean statusY = ScannerDialogs.promptContinue("answer me yes", "title");
		FormatUtils.printResult("StatusY", statusY);

		boolean statusN = ScannerDialogs.promptContinue("answer me no", "title");
		
		FormatUtils.printResult("StatusN", statusN);
		
		
	}

	public static boolean promptContinue(String message, String title) {
		Scanner scanner = new Scanner(System.in);

		FormatUtils.print(message + ">> [Enter]");
		String next = scanner.nextLine();
		return true;
	}

	private static boolean isFalseString(String response) {
		String resp2 = response.toLowerCase();
		return StringUtils.startsWith(resp2, "n") || StringUtils.startsWith(resp2, "0");
	}

}
