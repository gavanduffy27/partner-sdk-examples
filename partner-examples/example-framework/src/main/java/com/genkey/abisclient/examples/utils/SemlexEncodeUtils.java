package com.genkey.abisclient.examples.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class SemlexEncodeUtils {

	/**
	 * Generates template encoding in format used by SEMLEX.
	 * @param fingerId			NIST identifier of finger position
	 * @param referenceData		Raw encoding of referenceDataItem templates
	 * @param imageEncoding		Raw image encoding typically a wdq image
	 * @param format			Specified format typically "wsq"
	 * @return					Encoding of template as expected by SEMLEX Orbis system
	 * @throws IOException
	 */
	public static byte[] encodeSemlexFormat(int fingerId, byte [] referenceData, byte [] imageEncoding, String format) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte [] result=null;
		encodeNumberLE(dos, 420, 8);
		dos.writeByte(fingerId);
		dos.write(1);
		
		encodeNumberLE(dos, 500, 2);
		
		encodeByteLEArray(dos, format.getBytes());
		encodeByteLEArray(dos, imageEncoding);
		
		dos.writeByte(1);
		dos.write(referenceData);
		dos.close();
		result=bos.toByteArray();
		bos.close();
		return result;
	}
	
	
	public static byte getByte(long value, int index) {
		return (byte) (value >> (index * 8)); 
	}
	
	public static void encodeNumberLE(DataOutput output, long value, int nBytes) throws IOException  {
		for(int ix =0 ; ix < nBytes; ix++) {
			int byteValue = getByte(value, ix);
			output.writeByte(byteValue);
		}
	}
	
	public static void encodeByteLEArray(DataOutput output, byte [] data ) throws IOException {
		encodeNumberLE(output, data.length, 4);
		output.write(data);
	}
	
	
 	
	
}
