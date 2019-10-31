package main;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.junit.jupiter.api.Test;

import data.Range;
import essentials.PixelsHandler;
import essentials.Tools;

/**
 * a class that contains static fields used for inserting or retrieving an image's meta data
 * @author Eboubaker
 */
public class MetaDataExtractor {
	/**
	 * Those are the indexes for the reading offset for the image meta data
	 * -1 means changeable value
	 * 
	 * partIndex        : the index range of the part index
	 * lengthIndex      : the index range of the length of the file(only applies to the first part of the file)
	 * nameLengthIndex  : the index range of the file's name length(applies only for the first part)
	 * partsCountIndex  : the index range of the parts count(applies only for the first part)
	 * dataIndex        : the index range of the file's data (excluding the first part as it is changing by the name)
	 * nameIndex        : the index range of the file's name
	 */
	public static Range partIndex = new Range(0, 2),
						partsCountIndex = new Range(2, 4),
						dataIndex = new Range(2, -1),
						lengthIndex = new Range(4, 12),
						nameLengthIndex = new Range(12,13),
						nameIndex = new Range(13,-1);//will be set after reading the nameLengthIndex
						

	public static long getImageHolderSize(File img, int bitcount, int channels, int offest) {
		Dimension d = null;
		try {
			d = MetaDataExtractor.getImageDimensions(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d.width * d.height * channels * bitcount / 8 - offest;
	}
	public static void showPossibleModes(File imgfile, int offest, int unit) {
		
		Dimension d = null;
		try {
			d = MetaDataExtractor.getImageDimensions(imgfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 1; i <= 8; i++) {
			System.out.println("RGB  " + i + " bit: " + (d.width * d.height * 3 * i / 8 - offest)/unit + " units | RGBA :" + (d.width * d.height * 4 * i / 8 - offest)/unit + " units");
		}
	}
	public static Dimension getImageDimensions(File imgFile) throws IOException {
	  String suffix = Tools.getFileExtensionName(imgFile);
	  Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
	  while(iter.hasNext()) {
	    ImageReader reader = iter.next();
	    try {
	      ImageInputStream stream = new FileImageInputStream(imgFile);
	      reader.setInput(stream);
	      int width = reader.getWidth(reader.getMinIndex());
	      int height = reader.getHeight(reader.getMinIndex());
	      return new Dimension(width, height);
	    } catch (IOException e) {
	      System.err.println("Error reading: " + imgFile.getAbsolutePath());
	      e.printStackTrace();
	    } finally {
	      reader.dispose();
	    }
	  }
	  throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
	}
	@Test
	public static int readImagePartNumber(File imgfile, int bitcount, boolean useAlpha) throws IOException {
		int dataPixels = 1+(int)(((float)partIndex.end) / (bitcount * (useAlpha ? 4 : 3) / 8.0 ));// bitcount * (useAlpha ? 4 : 3) / 8 * 2
		Dimension d = getImageDimensions(imgfile);
		int w = dataPixels % d.width;
		int y = dataPixels / d.width;
		BufferedImage imgpart = PixelsHandler.readSubImage(imgfile, 0, 0, 1 + w, 1 + y);
		var datachunk = Extractor.extractRawData(imgpart, bitcount, useAlpha).subList(partIndex.start, partIndex.end);
		int partindex = datachunk.get(0) | (datachunk.get(1) << 0x0f);
		return partindex;
	}
	@Test
	public static int readTotalParts(File firstImageFile, int bitcount, boolean useAlpha) throws IOException {
		int dataPixels = 1+(int)(((float)partsCountIndex.end) / (bitcount * (useAlpha ? 4 : 3) / 8.0 ));// bitcount * (useAlpha ? 4 : 3) / 8 * 2
		Dimension d = getImageDimensions(firstImageFile);
		int w = dataPixels % d.width;
		int y = dataPixels / d.width;
		BufferedImage imgpart = PixelsHandler.readSubImage(firstImageFile, 0, 0, 1 + w, 1 + y);
		var datachunk = Extractor.extractRawData(imgpart, bitcount, useAlpha).subList(partsCountIndex.start, partsCountIndex.end);
		int partindex = datachunk.get(0) | (datachunk.get(1) << 0x0f);
		return partindex;
	}
}
