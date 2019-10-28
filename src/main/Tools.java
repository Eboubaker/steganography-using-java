package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class Tools{
	
//	public static short bytetoubyte(int byt) {
//		return (short)map(byt, -128, 127, 0, 255);
//	}
//	public static byte ubytetobyte(int ubyte) {
//		return (byte)map(ubyte, 0, 255, -128, 127);
//	}
	
	public static int CalculateRequiredInfoBytes(long pixelscount){//is it 255 or 255 * 4
		return (int)(Math.log(pixelscount) / Math.log(255)+1);
	}
	public static String inttobits(int i) {
		return Integer.toBinaryString(i);
	}
	
	public static float map(float value, float start1, float stop1, float start2, float stop2) {
		return ((value - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
	}
	public static byte[] longToBytes(long l) {
		byte[] ret = new byte[8];
		//16254
		for(int i = 7; i > -1; i--) {
			ret[i] = (byte) (l & 0xff);
			l = l >> 8;
			if(l == 0)
				break;
		}
		return ret;
	}
	//using ByteBuffer.wrap(bytes).getLong() will give the same results, but let's do it with the nerdish way.
	public static long bytesToLong(byte[] bytes) {
		if(bytes.length != 8)
			throw new UnsupportedOperationException("Long type can only hold 8 bytes, array length was: " + bytes.length);
		long res = 0;
		for(int i = 0; i < 8; i++) {
			if(bytes[i] > 0)
				res |= ((long) bytes[i]) << ((8-i) * 8 - 8);
			else {
				short val = bytes[i];
				val &= 0xff;
				res |= ((long) val) << ((8-i) * 8 - 8);
			}
		}
		return res;
	}
	public static long pow2(int exponent) {
		return 1 << exponent;
	}
	/**
	 * returns 2^count - 1
	 * if count  is 3, you will get 0b111<br>
	 * if it is 5 you will get 0b11111<br>
	 * and so on
	 */
	public static long fillbits(int count) {
		return (1 << count) - 1;
	}
	public static String decimalToBinary(long n, int bitsLength) {
		String s = Long.toBinaryString(n);
		if(s.length() != bitsLength) {
			String t = "";
			for(int i = bitsLength - s.length()-1; i > -1;i--)
				t += "0";
			t += s;
			s = t;
		}
		return s;
	}
	
	public static void createFileFromData(ArrayList<Byte> data, File output) throws IOException{
		var fos = new FileOutputStream(output);
		for(byte b: data)
			fos.write(b);
		fos.close();
	}
	/**
	 * will return empty string if the file has no extension
	 */
	public static String getFileExtensionName(File f) {
		String fname = f.getName();
		return fname.substring(fname.lastIndexOf('.')+1);
	}
}
