package com.genkey.abisclient.examples.utils;

/**
 * Helper class for object formatting.
 * @author Gavan
 *
 */
public class StringStream {
	
	StringBuilder builder = new StringBuilder();
	
	public static int DefaultFormatSize=15;
	
	int formatSize= DefaultFormatSize;
	
	
	public StringStream() {
		
	}
	
	public int getFormatSize() {
		return formatSize;
	}



	public void setFormatSize(int formatSize) {
		this.formatSize = formatSize;
	}



	public void appendChar(char ch) {
		builder.append(ch);
	}
	
	
	public void append(String text) {
		builder.append(text);
	}

	public void appendLine(String text) {
		append(text);
		nl();
	}
	
	
	public void nl() {
		appendChar('\n');
	}
	
	public void nls(int nLines) {
		String padString = makeString('\n', nLines);
		append(padString);
	}

	public void printLine(String text) {
		appendLine(text);
	}
	
	public void printAttribute(String attribute, Object value) {
		printAttribute(attribute, value, this.getFormatSize());
	}
	
	public void printAttribute(String attribute, Object value, int formatSize) {
		if (formatSize <= 0) {
			formatSize= this.getFormatSize();
		}
		int padSize = formatSize - attribute.length();
		String pad = "";
		if(padSize > 0) {
			pad = makeString(' ', padSize);
		}
		printLine( attribute + pad + "=" + formatObject(value));
	}
 	
	
	public void printObject(String header, Object value) {
		printHeader(header);
		printObject(value);
	}
	
	public void printHeader(String header) {
		printHeader(header, '=');
	}

	private void printHeader(String header, char ch) {
		String underScore = makeString(ch, header.length());
		printLine(header);
		printLine(underScore);
	}

	public void printObject(Object value) {
		 nl();
         printLine(formatObject(value));		
	}

	private String formatObject(Object value) {
		return value == null ? "(null)" : value.toString();
	}

	private static String makeString(char ch, int nChars) {
		char chars[] = new char[nChars];
		for(int ix = 0 ; ix < nChars ; ix++) {
			chars[ix] = ch;
		}
		return new String(chars);
	}
	
	
	@Override
	public String toString() {
		return this.builder.toString();
	}

}
