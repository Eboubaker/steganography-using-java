package test;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Test {
	public static void main99(String args[]) throws Exception {
		
		int a = 123, r = 231, g = 50, b = 16;
		int pix = PixelsHandler.argbtopixel(a, r, g, b);
		System.out.println(PixelsHandler.alpha(pix));
	}
	public static void main00(String args[]) throws Exception {
		File imageFile = new File("C:\\Users\\Public\\Pictures\\test.png");
		int alpha = 124, red = 248, green = 48, blue = 1;
		boolean usingAlpha = true;
		
		System.out.println("before saving the image: ");
		System.out.println("a:" + alpha + " r:" + red+ " g:" +green+ " b:" +blue);
		System.out.println();
		BufferedImage img1 = new BufferedImage(1,1,usingAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		int p0 = ARGBtoPixel(alpha, red, green, blue, usingAlpha);
		img1.setRGB(0, 0, p0);
		ImageIO.write(img1, "png", imageFile);
		
		/*.*******/
		
		BufferedImage img2 = ImageIO.read(imageFile);
		
//		Color pixel = new Color(img2.getRGB(0, 0), true);
//		System.out.println("after reading the image: ");
//		System.out.println("a:" + pixel.getAlpha() + " r:" + pixel.getRed()+ " g:" +pixel.getGreen()+ " b:" +pixel.getBlue());
		System.out.println(PixelsHandler.blue(PixelsHandler.getPixelsArray(img2)[0]));
		
	}
	public static int ARGBtoPixel(int alpha, int red, int green, int blue,boolean useAlpha) {
		if(!useAlpha)
			alpha = 255;
		int pixel =  (alpha << 24) | (red << 16) | (green << 8) | blue;
	    return pixel;
	}
	public static BufferedImage getImageFromArray(int[] pixels, int width, int height, int type) {
	    MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Image im = tk.createImage(mis);
	    BufferedImage bi = new BufferedImage(im.getWidth(null),im.getHeight(null), type);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
	    return bi;
	}
	public static void main3(String args[]) throws Exception {
		var arr = new ArrayList<Integer>();
		for(int i = -128; i < 128; i++) {
//			arr.add(main2(i));
		}
		for(int i = -128; i < 128; i++) {
			if(i != arr.get(i + 128))
			System.out.println(i + " -> " + arr.get(i+128) + " || " + Tools.decimalToBinary(i, 8) + " -> " + Tools.decimalToBinary(arr.get(i+128), 8));
		}
	}
	public static void main0(String[] args) throws Exception{
		File f = new File("E:\\Programming\\Processing\\clone\\src\\test.png");
		var imgChannelsData = new ArrayList<Integer> ();
		
		int bitcount = 3;
		{
			
			int t[] = new int[] {
								123,231,50,16,
								128,127,255,0
								};
			System.out.println("image data:");
			for(int i: t) {
				if(i > 0)
					System.out.print(String.format("%03d      , ", i));
				else
					System.out.print(String.format("%03d    , ", i));
			}
			System.out.println();
			for(int i: t) {
				System.out.print(String.format("%s , ", Tools.decimalToBinary(i, 8)));
			}
			System.out.println();
			int p1 = PixelsHandler.argbtopixel(t[0], t[1], t[2], t[3]);
			int p2 = PixelsHandler.argbtopixel(t[4], t[5], t[6], t[7]);
			int[] h = new int[] {
									PixelsHandler.alpha(p1),PixelsHandler.red(p1),PixelsHandler.green(p1),PixelsHandler.blue(p1),
									PixelsHandler.alpha(p2),PixelsHandler.red(p2),PixelsHandler.green(p2),PixelsHandler.blue(p2),
								};
			for(int c : h)
				imgChannelsData.add(c);
			
		}
		//insert
		{
			var filebytes = new ArrayList<ByteBits>();
			filebytes.add(new ByteBits(128));
			filebytes.add(new ByteBits(0));
			filebytes.add(new ByteBits(-127));
			System.out.println("file data:");
			for(var i: filebytes) {
				if(i.getFullValue() > 0)
					System.out.print(String.format("%03d      , ", i.getFullValue()));
				else
					System.out.print(String.format("%03d      , ", i.getFullValue()));
			}
			System.out.println();
			for(var i: filebytes) {
				System.out.print(String.format("%s , ", Tools.decimalToBinary(i.getFullValue(), 8)));
			}
			System.out.println();
			{
				int index = 0;
				int newval = imgChannelsData.get(index) >> bitcount;
				int i = 0;
				for(ByteBits b : filebytes) {
					while(b.hasNext()) {
						newval <<= 1;
						newval += b.next();
						i++;
						if(i == bitcount) {
							imgChannelsData.set(index, newval);
							if(index != imgChannelsData.size() - 1)
								index++;
							
							newval = imgChannelsData.get(index) >> bitcount;
							i = 0;
						}
					}
				}
			}
		}
		System.out.println("image data after processing:");
		for(int i: imgChannelsData) {
			if(i > 0)
				System.out.print(String.format("%-3d      , ", i));
			else
				System.out.print(String.format("%-3d    , ", i));
		}
		System.out.println();
		for(int i: imgChannelsData) {
			System.out.print(String.format("%s , ", Tools.decimalToBinary(i, 8)));
		}
		System.out.println();
		int a1 = 124, r1 = 248, g1 = 48, b1 = 1;
		int[] pixels = new int[] {PixelsHandler.argbtopixel(a1, r1, g1, b1)};
		ImageIO.write(PixelsHandler.getImageFromArray(pixels, 1, 1, BufferedImage.TYPE_INT_ARGB), "png", f);
		///extract
		{
			
			ArrayList<ByteBits> imgDataChunks = new ArrayList<>();
			int [] pix = null;
			{
				int[] t = PixelsHandler.getPixelsArray2(ImageIO.read(f));
				pix = new int [] {PixelsHandler.alpha(t[0]), PixelsHandler.red(t[0]), PixelsHandler.green(t[0]), PixelsHandler.blue(t[0])};
			}
			System.out.println("image data after reading:");
			for(int i: pix) {
				if(i > 0)
					System.out.print(String.format("%-3d      , ", i));
				else
					System.out.print(String.format("%-3d    , ", i));
			}
			System.out.println();
			for(int i: pix) {
				System.out.print(String.format("%s , ", Tools.decimalToBinary(i, 8)));
			}
			System.out.println();
			for(int c : pix) 
				imgDataChunks.add(new ByteBits(c, bitcount));
			ArrayList<java.lang.Short> storedData = new ArrayList<>();
			int value = 0;
			int index = 0;
			for(ByteBits b : imgDataChunks) {
				while(b.hasNext()) {
					value += b.next();
					if(index != 7) 
						value <<= 1;
					index++;
					if(index == 8) {
						storedData.add((short)(byte)value);
						index = 0;
						value = 0;
					}
				}
			}
//			return storedData.get(0);
			for(short t : storedData) {
				System.out.println(t);
//			}
		}
	}
	
	
	
}
}
class Mapper{
	static byte[] from = new byte[] {};
	static byte[] to = new byte[] {};
	public static byte map(byte b) {
		return 0;
	}
}
class ByteBits{
	public short b;
	private int index = 7;
	public ByteBits(int i) {
		b = (short) i;
	}
	public ByteBits(int i, int bitcount) {
		index = bitcount - 1;
		b = (short) i;
	}
	public boolean hasNext() {
		return index > -1;
	}
	/*public short next() {
		short n = (short) ((b&0b1000_0000) >> 7);
		n &= 0b0111_1111;
		b <<= 1;
		index --;
		return n;
	}*/
	public short next() {
		int n = (b&(1<<index))>>index;
		index --;
		return (short)n;
	}
	public short getFullValue() {
		return b;
	}
	
}
class PixelsHandler{
	public static BufferedImage getImageFromArray(int[] pixels, int width, int height, int type) {
	    MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Image im = tk.createImage(mis);
	    BufferedImage bi = new BufferedImage(im.getWidth(null),im.getHeight(null), type);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
	    return bi;
	}
	public static int argbtopixel(int alpha, int red, int green, int blue) {
		int ret =  (alpha << 24) | (red << 16) | (green << 8) | blue;
	    return ret;
	}
	public static int rgbtopixel(int red, int green, int blue) {
		int ret =  (255 << 24) | (red << 16) | (green << 8) | blue;
	    return ret;
	}
	public static int red(int pixel) {
		return (pixel >> 16) & 0xff;
	}
	public static int green(int pixel) {
		return (pixel >> 8) & 0xff;
	}
	public static int blue(int pixel) {
		return pixel & 0xff;
	}
	public static int alpha(int pixel) {
		return (pixel >> 24) & 0xff;
	}
	
	public static int[] getPixelsArray2(BufferedImage image) {
	      int width = image.getWidth();
	      int height = image.getHeight();
	      int[][] result = new int[height][width];

	      for (int row = 0; row < height; row++) {
	         for (int col = 0; col < width; col++) {
	            result[row][col] = image.getRGB(col, row);
	         }
	      }
	      int ret[] = new int[width*height];
	      for (int row = 0; row < height; row++) {
		         for (int col = 0; col < width; col++) {
		        	 int i = col + row * width;
		        	 ret[i] = result[row][col];
		         }
	      }
	      return ret;
	   }
public static int[] getPixelsArray(BufferedImage image) {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int[] result = new int[image.getWidth() * image.getHeight()];
		if (image.getAlphaRaster() != null) 
			for (int pixel = 0, index = 0; pixel + 3 < pixels.length; pixel += 4) {
				int argb = 0;
				argb += (int) (pixels[pixel] & 0xff) << 24; // alpha
				argb += (int) pixels[pixel + 1] & 0xff; // blue
	            argb += ((int) pixels[pixel + 2] & 0xff) << 8; // green
	            argb += ((int) pixels[pixel + 3] & 0xff) << 16; // red
	            result[index] = argb;
	            index++;
			}
		 else
			for (int pixel = 0,index = 0; pixel + 2 < pixels.length; pixel += 3) {
				int argb = -16777216;// 255 alpha
				argb += (int) pixels[pixel + 1] & 0xff; // blue
	            argb += ((int) pixels[pixel + 2] & 0xff) << 8; // green
	            argb += ((int) pixels[pixel + 3] & 0xff) << 16; // red
	            result[index] = argb;
	            index++;
			}
		
		return result;
	}
	public static int[] trimPixels(int [] pixels, int trimmer, int stop) {
		for(int i = 0; i < stop; i++) 
			pixels[i] = argbtopixel(alpha(pixels[i] & trimmer), red(pixels[i] & trimmer), green(pixels[i] & trimmer), blue(pixels[i] & trimmer));
		return pixels;
	}
}
class Tools{
	/*
	public static short bytetoubyte(int byt) {
		return (short)map(byt, -128, 127, 0, 255);
	}
	public static byte ubytetobyte(int ubyte) {
		return (byte)map(ubyte, 0, 255, -128, 127);
	}
	*/
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
	public static long bytesToLong(byte[] bytes) {
		/*ByteBuffer.wrap(bytes).getLong();*/
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
		if(s.length() < bitsLength) {
			String t = "";
			for(int i = bitsLength - s.length()-1; i > -1;i--)
				t += "0";
			t += s;
			s = t;
		}else {
			s = s.substring(s.length() - bitsLength);
		}
		return s;
	}
	
	public static void createFileFromData(ArrayList<Byte> data, File output) throws IOException{
		var fos = new FileOutputStream(output);
		for(byte b: data)
			fos.write(b);
	}
}

