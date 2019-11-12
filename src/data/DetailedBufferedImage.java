package data;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import essentials.PixelsHandler;
import essentials.Tools;
import main.Extractor;
import main.MetaDataHandler;

public class DetailedBufferedImage {
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
	private static final Range partIndex = new Range(0, 2),
						totalPartsIndex = new Range(2, 4),
						lengthIndex = new Range(4, 12),
						dataIndex = new Range(12, -1);
	
	
	private File imgfile;
	private BufferedImage img;
	private int bitcount, channels;
	private Dimension d;
	private Long holder = null, totalparts = null, index = null, dataLength = null;
	private List<Short> headData;
	
	public DetailedBufferedImage(File imgsrc, int bitcount, int channels) throws IOException {
		this.imgfile = imgsrc;
		this.bitcount = bitcount;
		this.channels = channels;
	}
	
	
	public BufferedImage getImage() throws IOException {
		if(img == null) {
			img = ImageIO.read(imgfile);
		}
		return img;
	}
	public File getFile() {
		return imgfile;
	}
	public BufferedImage getSubImage(Dimension d) throws IOException {
		if(img != null) {
			return img.getSubimage(0, 0, d.width, d.height);
		}else {
			return PixelsHandler.readSubImage(imgfile,d);
		}
		
	}
	
	
	public Dimension getImageDimensions() throws IOException {
		if(d != null)
			return d;
		Iterator<ImageReader> iter = ImageIO.getImageReaders(new FileImageInputStream(imgfile));
		while(iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgfile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return new Dimension(width, height);
			} catch (IOException e) {
				System.out.println("Reader Skipped");
			} finally {
				reader.dispose();
			}
		}
		throw new IOException("Wrong extension Format " + imgfile.getAbsolutePath());
	}
	public Long getPartIndex() throws IOException {
		if(index != null)
			return index;
		var datachunks = readBytesInRange(partIndex);
		Long partindex = Tools.bytesToLong(datachunks);
		return partindex;
	}
	public Long getTotalParts() throws IOException {
		if(totalparts != null)
			return totalparts;
		var datachunks = readBytesInRange(totalPartsIndex);
		Long totpartindex =  Tools.bytesToLong(datachunks);
		return totpartindex;
	}
	public Long getDataLength() throws IOException {
		if(dataLength != null)
			return dataLength;
		var datachunks = readBytesInRange(lengthIndex);
		Long lenidex = Tools.bytesToLong(datachunks);
		return lenidex;
	}
	public long getImageHolderSize(long offset) throws IOException {
		if(holder == null) {
			holder = (long) PixelsHandler.
					getImageHolderSize(getImageDimensions(), channels, bitcount);
		}
		return holder - offset;
	}
	private List<Short> readBytesInRange(Range range) throws IOException{
		if(headData == null) {
			int dataPixels = PixelsHandler.getRequiredPixels(dataIndex.end, bitcount, channels);
			Dimension imgd = getImageDimensions();
			Dimension reqd = PixelsHandler.getRequiredDimension(imgd, dataPixels);
			BufferedImage imgpart = PixelsHandler.readSubImage(imgfile,reqd);
			headData = Extractor.extractRawData(imgpart, bitcount, channels);
		}
		return headData.subList(range.start, range.end);
	}
}
