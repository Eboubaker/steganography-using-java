package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;

import javax.imageio.ImageIO;

public class PixelsHandler{
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
