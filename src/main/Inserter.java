package main;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import compression.Compressor;
import data.ByteBits;
import essentials.ChannelsHandler;
import essentials.PixelsHandler;
import essentials.Tools;

//TODO: 
//use arrays instead of ArrayList to increase the speed
/**
 * a class that handles inserting data to images
 * @author Eboubaker
 *
 */
public class Inserter {
	private File[] files;
	private File[] imgFiles;
	private File zipfile, outputfolder;
	private int bitcount;
	private Compressor com;
	private int channels;
	private List<File> imgfiles;
	private MetaDataHandler mdh;
	private static ArrayList<String> supported_image_types = new ArrayList<String>();
	private static final double MAXSIZE = 300 * 1024 * 1024;
	static {
			supported_image_types.add("png");
			supported_image_types.add("jpg");
			supported_image_types.add("jpeg");
	}
	/**
	 * 
	 * @param files only files
	 * @param imageFiles only files
	 * @param outputfolder only a folder
	 * @param bitcount any number
	 * @param channels any number
	 * @throws IOException
	 */
	public Inserter(File [] files, File[] imageFiles, File outputfolder, int bitcount, int channels) throws IOException {
		
		if(bitcount > 8 || bitcount < 1) 
			throw new UnsupportedOperationException("the given bitcount is " + bitcount + ", bitcount must be in the range of (1,8) (inclusive)");
		this.bitcount = bitcount;
		if(files == null || imageFiles == null)
			throw new NullPointerException("null input was given");
		System.out.println("Locating files ...");
		this.files = Tools.getFilesList(files).toArray(new File[] {});
		//filter all non images files or non supported images files
		System.out.println("Locating images ...");
		imgfiles = Tools.getFilesList(imageFiles);
		if(imgfiles.removeIf(file -> !supported_image_types.contains(Tools.getFileExtensionName(file).toLowerCase())))
			System.out.println("some images were removed from the list, because they are non image files or non supported image files");
		if(imgfiles.size() == 0)
			throw new NullPointerException("no supported images were found in the given list");
		
		this.channels = channels;
		mdh = new MetaDataHandler(bitcount, channels);
		this.outputfolder = new File(outputfolder.getAbsolutePath() + File.separator + "Hidden Data" + System.nanoTime());
	}
	
	public void insertData() throws OutOfImageSpaceException, IOException {
		System.out.println("Started Operation: Insertion");
		System.out.println("Compressing data...");
		zipfile = File.createTempFile("temp", ".brh");
		this.com = new Compressor(zipfile);
		com.setShowProgress(true, 1000);
		com.compress(files);
		zipfile = new File(zipfile.getAbsolutePath());
//		zipfile = new File("C:\\Users\\MCS\\AppData\\Local\\Temp\\temp11658422161116061663.brh");
		System.out.println("Preparing & filtering images ...");
		
		Collections.sort(imgfiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				Dimension d1 = null;
				Dimension d2 = null;
				try {
					d1 = MetaDataHandler.getImageDimensions(f1);
					d2 = MetaDataHandler.getImageDimensions(f2);
				} catch (IOException e) {
				}
				return d1.height * d1.width - d2.height * d2.width;
			}
		}.reversed());
//		for(int i = 0; i < imgfiles.size();i++) {
//			for(int j = 0; j < i - 1;j++) {
//				Dimension d1 = MetaDataHandler.getImageDimensions(imgfiles.get(j));
//				Dimension d2 = MetaDataHandler.getImageDimensions(imgfiles.get(j+1));
//				if(d1.width * d1.height < d2.width * d2.height) {
//					File temp = imgfiles.get(j);
//					imgfiles.set(j, imgfiles.get(j+1));
//					imgfiles.set(j+1, temp);
//				}
//			}
//		}
		var holders = new ArrayList<Long>(imgfiles.size());
		long total = 0;
		for(int i = 0; i < imgfiles.size(); i++) {
			Dimension d = MetaDataHandler.getImageDimensions(imgfiles.get(i));
			double size = (double)d.width * (double)d.height * (double)this.channels;
			if(size> MAXSIZE) {
				double perc = MAXSIZE/size;
				d.width *= 1 - perc;
				d.height *= 1 - perc;
			}
			long h = PixelsHandler.getImageHolderSize(d, this.bitcount, this.channels) - MetaDataHandler.dataIndex.start;
			holders.add(h);
			total += h;
		}
		if (total < zipfile.length())
			throw new OutOfImageSpaceException(total, zipfile.length());
		
		long inslen = 0;
		int totalparts = 0;
		while(inslen < zipfile.length()) {
			if (totalparts == imgfiles.size())
				throw new OutOfImageSpaceException(inslen, zipfile.length());
				Dimension d = MetaDataHandler.getImageDimensions(imgfiles.get(totalparts));
				double size = (double)d.width * (double)d.height * (double)this.channels;
				if(holders.get(totalparts) > zipfile.length() && holders.get(totalparts) * .8f < zipfile.length()) {
					inslen += holders.get(totalparts); 
					totalparts++;
					continue;
				}
				if(size > MAXSIZE && totalparts+1 != imgfiles.size() && total - holders.get(totalparts+1)  >= zipfile.length()) {
					total -= holders.get(totalparts);
					holders.remove(totalparts);
					imgfiles.remove(totalparts);
					continue;
				}
				if(holders.get(totalparts) > zipfile.length() && totalparts+1 != imgfiles.size() && total - holders.get(totalparts+1) >= zipfile.length()) {
					total -= holders.get(totalparts);
					holders.remove(totalparts);
					imgfiles.remove(totalparts);
					continue;
				}
				inslen += holders.get(totalparts);
				totalparts++;
		}
		imgfiles.subList(totalparts, imgfiles.size()).clear();
		imgFiles = imgfiles.toArray(new File[imgfiles.size()]);
		
		//will throw exception if the capacity of images is not enough
		System.out.println("Checking images capacity ...");
		checkCapacity();
		FileInputStream is = new FileInputStream(zipfile);
		byte[] totalpartsbytes = Arrays.copyOfRange(Tools.longToBytes(totalparts), 8 - MetaDataHandler.totalPartsIndex.length(), 8);
		if(!outputfolder.exists())
			outputfolder.mkdirs();
		
		long totalread = 0;
		int dataoffset = MetaDataHandler.dataIndex.start;
		for (int n = 0; n < imgFiles.length; n++) {
			System.gc();
			File imgf = imgFiles[n];
			System.out.println("opening img" + n + " ...");
			BufferedImage img = ImageIO.read(imgf);
			Dimension d = new Dimension(img.getWidth(), img.getHeight());
			double size = d.width * d.height * this.channels;
			if( size> MAXSIZE) {
				System.out.println(String.format("img%d will be resized because it is large to handle", n));
				double perc = 1 - MAXSIZE/size;
				img = PixelsHandler.resize(img, perc);
				d = new Dimension(img.getWidth(), img.getHeight());
			}
			int imgholdersize = PixelsHandler.getImageHolderSize(d, this.bitcount, this.channels);
			byte[] buffer = null;
			
			if(imgholdersize > zipfile.length() - totalread) 
				buffer = new byte[(int) (zipfile.length() - totalread)];
			else 
				buffer = new byte[imgholdersize - dataoffset];
			System.out.println("Reading next data chunks ...");
			int read = is.read(buffer);
			//is.read(b, 12, len)
			buffer = Tools.rshiftArray(buffer, MetaDataHandler.dataIndex.start);
			if(read > 0) {
				byte[] partlen = Arrays.copyOfRange(Tools.longToBytes(n), 8 - MetaDataHandler.partIndex.length(), 8);
				byte[] readlen = Arrays.copyOfRange(Tools.longToBytes(read), 8 - MetaDataHandler.lengthIndex.length(), 8);
				
				for(int i = MetaDataHandler.partIndex.start; i < MetaDataHandler.partIndex.start + partlen.length;i++)
					buffer[i] = partlen[i-MetaDataHandler.partIndex.start];
				for(int i = MetaDataHandler.totalPartsIndex.start; i < MetaDataHandler.totalPartsIndex.start + totalpartsbytes.length;i++)
					buffer[i] = totalpartsbytes[i-MetaDataHandler.totalPartsIndex.start];
				for(int i = MetaDataHandler.lengthIndex.start; i < MetaDataHandler.lengthIndex.start + readlen.length;i++)
					buffer[i] = readlen[i-MetaDataHandler.lengthIndex.start];
				
				
				System.out.println("copying ...");
//				BufferedImage newimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
//				newimg.getGraphics().drawImage(img, 0, 0, null);
//				img = newimg;
				System.out.println("inserting data for img" + n + " ...");
				img = insertData2(imgFiles[n], img, buffer);
				
//				img = null;
				System.gc();
				System.out.println("saving img" + n + " ...");
				String name = imgf.getName();
				name = name.substring(0, name.lastIndexOf('.'))+".png";
				ImageIO.write(img, "png", new File(outputfolder.getAbsolutePath() + File.separator + name));
//				ImageIO.write(newimg, "png", new File(outputfolder.getAbsolutePath() + File.separator + name));
				//TODO: why create a new buffered image instead of writing to the existing one ?
				//because they are not the same type you idiot, TYPE_INT_ARGB  != TYPE_INT_RGB
			}
		}
		is.close();
		
		System.out.println("Generating \"Info.txt\" ...");
		generateInfoFile();
		zipfile.delete();
		System.out.println("Operation: Insertion, compleated, output : " + outputfolder.getAbsolutePath());
	}

	private void generateInfoFile() throws FileNotFoundException {
		PrintStream p = new PrintStream(new File(outputfolder.getAbsolutePath() + File.separator + "Info.txt"));
		p.println("You May Delete This File if You Want,");
		p.println("This File Only Contains settings used for these image & the list of Files Hidden in These Images ");
		p.println();
		p.println("bitcount used: " + this.bitcount + " , channels used: " + this.channels);
		p.println();
		p.println("data in these images : " + String.format("%,dKB (%,dKB Compressed)", Tools.getFilesSize(files) / 1024, zipfile.length()/1024));
		p.println();
		p.println("file list :");
		for(var f : files) {
			String name = f.getName();
			String path = f.getAbsolutePath();
			int sind = path.indexOf(name)-1;
			printfiles(p, sind, f);
		}
		p.close();
	}
	
	
	
	
	
	
	public BufferedImage insertData2(File imgf, BufferedImage img, byte[] data) throws IndexOutOfBoundsException, IOException, OutOfImageSpaceException{
		img.setAccelerationPriority(1);
		var newimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		newimg.setAccelerationPriority(1);
		
		ChannelsHandler channelshandler = new ChannelsHandler(img, newimg, this.channels);
		
		byte cubyte = 0;
		int remainingbits = 0, len = 0;
		boolean no_more_data = false;
		while(!no_more_data) {
			int bit_index = 0;
			int newval = channelshandler.next() >> this.bitcount;
			while(bit_index < this.bitcount) {
				if(--remainingbits == -1) {
					if(len == data.length) {
						no_more_data = true;
						cubyte = (byte)(Math.random() * 0xff);
					}else {
						cubyte = data[len++];
						remainingbits = 7;//8-1 range(0,7)inclu
					}
				}
				newval <<= 1;
				newval += (cubyte >> remainingbits) & 1;
				bit_index++;
//				databyte <<= 1;
			}
			channelshandler.add(newval);
		}
		while(channelshandler.hasNext()) {
			int n = ((channelshandler.next() >> this.bitcount)<<this.bitcount) | ( (int)(Math.random() * ((1<<this.bitcount)-1)) );
			channelshandler.add(n);
		}
		return newimg;
//		channelshandler.reset();
//		for(int i = 0; i < data.length; i++) {
//			
//		}
	}
	
	
	public BufferedImage insertData(File imgf, BufferedImage img, byte[] data) throws IndexOutOfBoundsException, IOException, OutOfImageSpaceException{
		
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		newimg.setAccelerationPriority(1);
		var databytes = new ArrayList<ByteBits>(data.length);
		for(byte b: data)
			databytes.add(new ByteBits(b));
		
		int[] imgpixels= PixelsHandler.getPixelsArray2(img);
//		else {
//			int reqpix = PixelsHandler.getRequiredPixels(data.length, this.bitcount, this.channels);
//			var reqdim = PixelsHandler.getRequiredDimension(new Dimension(img.getWidth(), img.getHeight()), reqpix);
//			BufferedImage subimg = PixelsHandler.readSubImage(imgf, reqdim);
//			imgpixels = PixelsHandler.getPixelsArray2(subimg);
//		}
		var imgChannelsData = new ArrayList<Integer>(imgpixels.length*this.channels);
		if(this.channels == 4)
			for(int pix : imgpixels) {
				imgChannelsData.add(PixelsHandler.red(pix));
				imgChannelsData.add(PixelsHandler.green(pix));
				imgChannelsData.add(PixelsHandler.blue(pix));
				imgChannelsData.add(PixelsHandler.alpha(pix));
			}
		else
			for(int pix : imgpixels) {
				imgChannelsData.add(PixelsHandler.red(pix));
				imgChannelsData.add(PixelsHandler.green(pix));
				imgChannelsData.add(PixelsHandler.blue(pix));
			}
		
		imgpixels = null;
		for(int i = databytes.size() * 8 / this.bitcount; i < imgChannelsData.size();i ++)
			imgChannelsData.set(i, ((imgChannelsData.get(i) >> this.bitcount)<<this.bitcount) | ((byte)(Math.random() * Tools.fillbits(this.bitcount))));
		
		int index = 0;
		int newval = imgChannelsData.get(index) >> this.bitcount;
		int bit_index = 0;
//		System.out.println(channels + " " + imgChannelsData.size() + " " + databytes.size());
		for(int i = 0; i < databytes.size(); i++) {
			var b = databytes.get(i);
			while(b.hasNext()) {
				newval <<= 1;
				newval += b.next();
				bit_index++;
				if(bit_index == this.bitcount) {
					imgChannelsData.set(index, newval);
					index++;
					if(index % this.channels == 0) {
						int ind = (index-1) / this.channels;
						int x = ind % w;
						int y = ind / w;
						if(this.channels == 4) {
							newimg.setRGB(x, y, PixelsHandler.argbtopixel(imgChannelsData.get(index - 1), imgChannelsData.get(index - 4), imgChannelsData.get(index - 3), imgChannelsData.get(index-2)));
						}else {
							newimg.setRGB(x, y, PixelsHandler.rgbtopixel(imgChannelsData.get(index - 3), imgChannelsData.get(index - 2), imgChannelsData.get(index-1)));
						}
					}
					if(index != imgChannelsData.size()) {
						newval = imgChannelsData.get(index) >> bitcount;
					}
					bit_index = 0;
				}
			}
		}
		//complete the other section of the image
		for(;index < imgChannelsData.size(); index++) {
			int ind = (index-1) / this.channels;
			int x = ind % w;
			int y = ind / w;
			if(this.channels == 4)
				newimg.setRGB(x, y, PixelsHandler.argbtopixel(imgChannelsData.get(index - 1), imgChannelsData.get(index - 4), imgChannelsData.get(index - 3), imgChannelsData.get(index-2)));
			else
				newimg.setRGB(x, y, PixelsHandler.rgbtopixel(imgChannelsData.get(index - 3), imgChannelsData.get(index - 2), imgChannelsData.get(index-1)));
		}
		return newimg;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void printfiles(PrintStream p, int subNameInd, File file) {
		if(file.isFile())
			p.println(file.getAbsolutePath().substring(subNameInd));
		else {
			File[] lis = file.listFiles();
			if(lis == null)
				return;
			for(var f: lis)
				printfiles(p, subNameInd, f);
		}
	}
	
	
	public void checkCapacity() throws OutOfImageSpaceException, IOException {
		long capacity = 0;
		for(File im : imgFiles)
			capacity += mdh.getImageHolderSize(im, PixelsHandler.getRequiredPixels(MetaDataHandler.dataIndex.start, bitcount, channels));
		long len = zipfile.length();
		if(capacity < len) {
			System.out.println("these files are too large for the selected images, use higher bitcount or add more images to the list");
			throw new OutOfImageSpaceException(capacity, len);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public BufferedImage insertData(File imgfile, File data, File output, int bitcount, boolean useAlpha) throws IndexOutOfBoundsException, IOException, OutOfImageSpaceException{
		int imgChannels = useAlpha ? 4 : 3;
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
		BufferedImage img = ImageIO.read(imgfile);
		
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		int[] imgpixels = PixelsHandler.getPixelsArray2(img);
		//how much bytes can the image hold:
		long imgContainerSize = imgpixels.length * imgChannels * bitcount / 8;//in bytes
		//the total data that will be inserted into the image
		// + 4 bytes for the length of the file(long)
		long dataLength = data.length() + 4;
		System.out.println(imgContainerSize);
		if(dataLength > imgContainerSize)
			throw new OutOfImageSpaceException(imgContainerSize, dataLength);
		
		var databytes = new ArrayList<ByteBits>((int) (data.length() + 4));
		byte[] sizeAsBytes = Tools.longToBytes(data.length());
		//we add the length of the file to the data(so we can know when to stop reading when we are extracting the data)
		for(byte b: sizeAsBytes) 
			databytes.add(new ByteBits(b));
		//then add the file bytes
		try(var is = new FileInputStream(data);){
			for(int i : is.readAllBytes()) 
				databytes.add(new ByteBits(i));
		}
//		long requiredPixels = (long)(1 + databytes.size() * 8.0f / bitcount / imgChannels);
		var imgChannelsData = new ArrayList<Integer>(imgpixels.length*imgChannels);
		if(useAlpha)
			for(int pix : imgpixels) {
				imgChannelsData.add(PixelsHandler.red(pix));
				imgChannelsData.add(PixelsHandler.green(pix));
				imgChannelsData.add(PixelsHandler.blue(pix));
				imgChannelsData.add(PixelsHandler.alpha(pix));
			}
		else
			for(int pix : imgpixels) {
				imgChannelsData.add(PixelsHandler.red(pix));
				imgChannelsData.add(PixelsHandler.green(pix));
				imgChannelsData.add(PixelsHandler.blue(pix));
			}
		for(int i = databytes.size() / bitcount; i < imgChannelsData.size();i ++)
			imgChannelsData.set(i, ((imgChannelsData.get(i) >> bitcount)<<bitcount) | ((byte)(Math.random() * Tools.fillbits(bitcount))));
		{
			int index = 0;
			int newval = imgChannelsData.get(index) >> bitcount;
			int bit_index = 0;
			for(ByteBits b : databytes) {
				while(b.hasNext()) {
					newval <<= 1;
					newval += b.next();
					bit_index++;
					if(bit_index == bitcount) {
						imgChannelsData.set(index, newval);
						index++;
						if(index % imgChannels == 0) {
							int ind = (index-1) / imgChannels;
							int x = ind % w;
							int y = ind / w;
							if(useAlpha)
								newimg.setRGB(x, y, PixelsHandler.argbtopixel(imgChannelsData.get(index - 1), imgChannelsData.get(index - 4), imgChannelsData.get(index - 3), imgChannelsData.get(index-2)));
							else
								newimg.setRGB(x, y, PixelsHandler.rgbtopixel(imgChannelsData.get(index - 3), imgChannelsData.get(index - 2), imgChannelsData.get(index-1)));
						}
						newval = imgChannelsData.get(index) >> bitcount;
						bit_index = 0;
					}
				}
			}
			for(;index < imgpixels.length * imgChannels; index++) {
				int ind = (index-1) / imgChannels;
				int x = ind % w;
				int y = ind / w;
				if(useAlpha)
					newimg.setRGB(x, y, PixelsHandler.argbtopixel(imgChannelsData.get(index - 1), imgChannelsData.get(index - 4), imgChannelsData.get(index - 3), imgChannelsData.get(index-2)));
				else
					newimg.setRGB(x, y, PixelsHandler.rgbtopixel(imgChannelsData.get(index - 3), imgChannelsData.get(index - 2), imgChannelsData.get(index-1)));
			}
		}
//		int[] newpixels = new int[img.getHeight() * img.getWidth()];
//		int max = img.getWidth() * img.getHeight() * imgChannels;
//		if(useAlpha){
//			for(int i = 0;i < max; i += 4) {
////				System.out.print(String.format("%03d,", imgChannelsData.get(i/4)));
//				newpixels[i / 4] = PixelsHandler.argbtopixel(imgChannelsData.get(i+3), imgChannelsData.get(i), imgChannelsData.get(i+1), imgChannelsData.get(i+2));
//			}
//		}else {
//			for(int i = 0;i < max; i += 3) {
//					newpixels[i / 3] = PixelsHandler.rgbtopixel(imgChannelsData.get(i), imgChannelsData.get(i+1), imgChannelsData.get(i+2));
//			}
//		}
//		for(int i = 0; i < newpixels.length; i++) {
//			int x = i % w;
//			int y = i / w;
//			newimg.setRGB(x, y, newpixels[i]);
//		}
		ImageIO.write(newimg, "png", output);
		return newimg;
	}
	void saveImageAsJPEG(BufferedImage image,
		      OutputStream stream, int qualityPercent) throws IOException {
		    if ((qualityPercent < 0) || (qualityPercent > 100)) {
		      throw new IllegalArgumentException("Quality out of bounds!");
		    }
		    float quality = qualityPercent / 100f;
		    ImageWriter writer = null;
		    var iter = ImageIO.getImageWritersByFormatName("jpg");
		    if (iter.hasNext()) {
		      writer = (ImageWriter) iter.next();
		    }
		    ImageOutputStream ios = ImageIO.createImageOutputStream(stream);
		    writer.setOutput(ios);
		    ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
		    iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		    iwparam.setCompressionQuality(quality);
		    writer.write(null, new IIOImage(image, null, null), iwparam);
		    ios.flush();
		    writer.dispose();
		    ios.close();
		  }
	
}




