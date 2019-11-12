package essentials;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class PixelsHandler{
	public static int getRequiredPixels(int bytescount, int bitcount, int channels) {
		float val = 1.0f * bytescount * 8 / bitcount / channels;
		return val - (int) (val) == 0 ? (int) val: (int)(val + 1);
		
	}
	
	public static Dimension getRequiredDimension(Dimension image_dimensions, int pixelsLength) {
		int h;
		if(pixelsLength % image_dimensions.width == 0) {
			h = pixelsLength / image_dimensions.width;
		}else {
			h = (int) ((float)(pixelsLength) / image_dimensions.width + 1);
		}
		return new Dimension(image_dimensions.width, h);
	}
	
	public static int getImageHolderSize(Dimension d, int bitcount, int channels) {
		long l = d.height * d.width;
		l = l * channels * bitcount / 8; 
		return (int) l;
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
	
	public static BufferedImage readSubImage(File img, Dimension d) throws IOException {
//		w+=h;h=w-h;w=w-h;//swap
		Rectangle sourceRegion = new Rectangle(0, 0, d.width, d.height); // The region  to extract
		ImageInputStream stream = ImageIO.createImageInputStream(img); // File or input stream
		Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
		if (readers.hasNext()) {
		    ImageReader reader = readers.next();
		    reader.setInput(stream);
		   
		    ImageReadParam param = reader.getDefaultReadParam();// we get the read parameter
		    param.setSourceRegion(sourceRegion); // Set region
	
		    BufferedImage image = reader.read(0, param); // Will read only the region specified
		    return image;
		}
		//if we reached here it means there is an error.
		throw new IOException("Not a known image or a corrupt file: " + img.getAbsolutePath());
	}
	
	public static BufferedImage resize(BufferedImage inputImage, double percent) throws IOException {
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return outputImage;
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

	
}
