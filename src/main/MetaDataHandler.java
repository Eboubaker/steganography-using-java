package main;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;



import data.Range;
import essentials.PixelsHandler;
import essentials.Tools;

/**
 * a class that contains static fields used for inserting or retrieving an image's meta data
 * @author Eboubaker
 */
public class MetaDataHandler {
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
	public static final Range partIndex = new Range(0, 2),
						totalPartsIndex = new Range(2, 4),
						lengthIndex = new Range(4, 12),
						dataIndex = new Range(12, -1);
//						nameLengthIndex = new Range(12,13),
//						nameIndex = new Range(13,-1);//will be set after reading the nameLengthIndex
	private int bitcount, channels;
	
	
	public MetaDataHandler(int bitcount, int channels) {
		this.bitcount = bitcount;
		this.channels = channels;
	}
	
	public long getImageHolderSize(File img, int offset) throws IOException {
		return PixelsHandler.getImageHolderSize(getImageDimensions(img), channels, bitcount);
	}
	
	public void showPossibleModes(File imgfile, int offest, int unit) {
		
		Dimension d = null;
		try {
			d = getImageDimensions(imgfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 1; i <= 8; i++) {
			System.out.println("RGB  " + i + " bit: " + (d.width * d.height * 3 * i / 8 - offest)/unit + " units | RGBA :" + (d.width * d.height * 4 * i / 8 - offest)/unit + " units");
		}
	}
	public static Dimension getImageDimensions2(File imgFile) throws IOException {
//		  String suffix = Tools.getFileExtensionName(imgFile);
		byte[] data = new byte[32];
		int width, height;
		RandomAccessFile fis = new RandomAccessFile(imgFile, "r");
		Dimension ret = null;
		fis.read(data);
		if( data[0] == -119
			&& data[1] == 80
			&& data[2] == 78
			&& data[3] == 71
			) {
			//PNG
			width  =  (int) Tools.bytesToLong(Arrays.copyOfRange(data, 16, 20));
			height =  (int) Tools.bytesToLong(Arrays.copyOfRange(data, 20, 24));
			ret =  new Dimension(width, height);
		}else if( 	   data[0] == -1//0xff
					&& data[1] == -40//0xc8
				) {
			//JPG/JPEG
			fis.seek(2);
//			long len = imgFile.length() - 2;
			long read = 0;
			while(read < 4096/2) {//cuz we increment by one
				char c1 = getchar(fis.read());
				char c2 = getchar(fis.read());
				if(c1 == 0xff && (c2 == 0xc0/*|| c2 == 0xc1*/)) {
					fis.skipBytes(10);// 4 + 6
					width  =  (int) Tools.bytesToLong(new byte[] {(byte) fis.read(), (byte) fis.read()});
					height =  (int) Tools.bytesToLong(new byte[] {(byte) fis.read(), (byte) fis.read()});
					ret =  new Dimension(width, height);
				}
				read ++;
			}
		}else 
			ret = getImageDimensions2(imgFile);
		fis.close();
		return ret == null ? ret : getImageDimensions2(imgFile);
	}
	private static char getchar(int b) {
		return (char)(0xff & b);
	}
	public static Dimension getImageDimensions(File imgFile) throws IOException {
//	  String suffix = Tools.getFileExtensionName(imgFile);
	  
	  Iterator<ImageReader> iter = ImageIO.getImageReaders(new FileImageInputStream(imgFile));
	  while(iter.hasNext()) {
	    ImageReader reader = iter.next();
	    try {
	      ImageInputStream stream = new FileImageInputStream(imgFile);
	      reader.setInput(stream);
	      int width = reader.getWidth(reader.getMinIndex());
	      int height = reader.getHeight(reader.getMinIndex());
	      return new Dimension(width, height);
	    } catch (IOException e) {
//	      System.err.println("Error reading: " + imgFile.getAbsolutePath());
//	      e.printStackTrace();
	    } finally {
	      reader.dispose();
	    }
	  }
	  throw new IOException("Wrong extension Format " + imgFile.getAbsolutePath());
	}
	public int readPartIndex(File imgfile) throws IOException {
		var datachunks = readBytesInRange(imgfile, partIndex);
		int partindex = (int) Tools.bytesToLong(datachunks);
		return partindex;
	}
	public int readTotalParts(File imgfile) throws IOException {
		var datachunks = readBytesInRange(imgfile, totalPartsIndex);
		int totpartindex = (int) Tools.bytesToLong(datachunks);
		return totpartindex;
	}
	public int readDataLength(File imgfile) throws IOException {
		var datachunks = readBytesInRange(imgfile, lengthIndex);
		int lenidex = (int) Tools.bytesToLong(datachunks);
		return lenidex;
	}
	private List<Short> readBytesInRange(File image, Range range) throws IOException{
		int dataPixels = PixelsHandler.getRequiredPixels(range.end, bitcount, channels);
		Dimension imd = getImageDimensions(image);
		Dimension d = PixelsHandler.getRequiredDimension(imd, dataPixels);
		
		BufferedImage imgpart = PixelsHandler.readSubImage(image,d);
		
		return Extractor.extractRawData(imgpart, bitcount, channels).subList(range.start, range.end);
	}
	
}
