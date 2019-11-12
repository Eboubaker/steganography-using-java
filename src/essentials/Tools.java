package essentials;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Tools{
	private final static String PNG = "89504e47", JPG = "ffd8", GIF = "47494638", BMB = "424d";
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
	
//	public static float map(float value, float start1, float stop1, float start2, float stop2) {
//		return ((value - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
//	}
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
	public static long getFilesSize(File ...files) {
		long ret = 0;
		if(files == null)
			return 0;
		for(var f : files) {
			if(f.isDirectory()) {
				ret += getFilesSize(f.listFiles());
			}else {
				ret += f.length();
			}
		}
		return ret;
	}
	//using ByteBuffer.wrap(bytes).getLong() will give the same results, but let's do it with the nerdish way.
	public static long bytesToLong(byte[] bytes) {
		long ret = 0;
		int index = 0;
		for(int i = bytes.length-1; i != -1; i--) {
			ret |= (0xff&bytes[i]) << (8 * index);
			index++;
		}
		return ret;
	}
	public static long bytesToLong(List<Short> bytes) {
		if(bytes.size() > 8)
			throw new UnsupportedOperationException("Long type can only hold 8 bytes, array length was: " + bytes.size());
		long res = 0;
		for(int i = 0; i < bytes.size(); i++) {
			res |= ((long) (bytes.get(i)&0xff)) << ((bytes.size()-i) * 8 - 8);
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
	public static void printChannelsData(Object imgsrc, File output) throws IOException {
		BufferedImage img;
		if(imgsrc instanceof File) {
			img = ImageIO.read((File) imgsrc);
		}else 
			img = (BufferedImage) imgsrc;
		boolean hasalpha = img.getColorModel().hasAlpha();
		int w = img.getWidth();
		int len = w * img.getHeight();
		int index = 0;
		try(var fos = new FileOutputStream(output);){
			while(index < len) {
				int x = index % w;
				int y = index / w;
				int pixel = img.getRGB(x, y);
				fos.write(PixelsHandler.red(pixel));
				fos.write(PixelsHandler.green(pixel));
				fos.write(PixelsHandler.blue(pixel));
				if(hasalpha)
					fos.write(PixelsHandler.alpha(pixel));
				index ++;
			}
		}
	}
	/**
	 * 
	 * @param folders some folders or files or combination of both
	 * @return list of the files and files contained in these directories 
	 */
	public static List<File> getFilesList(File ...folders){
		ArrayList<File> list = new ArrayList<File>();
		if(folders == null)
			return list;
		for(var file: folders) {
			if(file.isDirectory())
				list.addAll(getFilesList(file.listFiles()));
			else if(file.isFile()) 
				list.add(file);
		}
		return list;
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
	public static byte[] rshiftArray(byte[] arr, int rshift) {
		byte[] newarr = new byte[arr.length + rshift];
		for(int i = rshift; i < newarr.length; i++)
			newarr[i] = arr[i-rshift];
		return newarr;
	}
	public static File[] PromptSelectFiles(String prompt, File home, FileFilter filt, boolean multiSelection, int choosemode) {
		if(prompt == null)
			prompt = "choose file(s)";
		JFileChooser fc = new JFileChooser(home);
		fc.addChoosableFileFilter(filt);
		fc.setMultiSelectionEnabled(multiSelection);
		fc.setFileHidingEnabled(false);
		fc.setFileSelectionMode(choosemode);
		fc.setDialogTitle(prompt);
		Action details = fc.getActionMap().get("viewTypeDetails");
		details.actionPerformed(null);
		int selres = fc.showOpenDialog(null);
		File[] retlist = null;
		File ret = null;
		if(selres == JFileChooser.APPROVE_OPTION) {
			if(multiSelection)
				retlist = fc.getSelectedFiles();
			else
				ret = fc.getSelectedFile();
		}
		return multiSelection ? retlist : new File[] {ret};
	}
}
