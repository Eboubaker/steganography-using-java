package main;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import compression.Compressor;
import data.ByteBits;
import essentials.ChannelsHandler;
import essentials.PixelsHandler;
import essentials.Tools;

public class Extractor {
	private File[] imgFiles;
	private File outputfolder;
	private int bitcount;
	private Compressor com;
	private int channels;
	private List<File> imgfiles;
	private HashMap<Integer, File> imgsmap;
	private static final ArrayList<String> supported_image_types = new ArrayList<String>();
	private MetaDataHandler mdh;
	static {
			supported_image_types.add("png");
			//supported_image_types.add("jpg");
			//supported_image_types.add("jpeg");
	}
	public Extractor(File[] imageFiles, File outputfolder, int bitcount, int channels) throws IOException {
		System.out.println("Selected Operation: Extraction");
		System.out.println("Checking pre-conditions...");
		if(bitcount > 8 || bitcount < 1) 
			throw new UnsupportedOperationException("the given bitcount is " + bitcount + ", bitcount must be in the range of (1,8) (inclusive)");
		this.bitcount = bitcount;
		if(channels != 4 && channels != 3)
			throw new UnsupportedOperationException(channels + " channel images are not supported only 3 or 4 are accepted");
		this.channels = channels;
		mdh = new MetaDataHandler(bitcount, channels);
		if(imageFiles == null)
			throw new NullPointerException("null input was given");
		System.out.println("Locating files...");
		imgfiles = Tools.getFilesList(imageFiles);
		//filter all non images files or non supported images files
		if(imgfiles.removeIf(file -> !supported_image_types.contains(Tools.getFileExtensionName(file).toLowerCase())))
			System.out.println("some images were removed from the list, because they are non image files or non supported image files");
//		for(int i = 0; i < imgfiles.size(); i++) {
//			try {
//				MetaDataHandler.getImageDimensions(imgfiles.get(i));
//			}catch(IOException e) {
//				imgfiles.remove(i);
//				System.out.println(String.format("Image was skipped: %s ,its format (%s)is not correct", imgfiles.get(i).getName(), Tools.getFileExtensionName(imgfiles.get(i))));
//			}
//		}
		if(imgfiles.size() == 0)
			throw new NullPointerException("no supported images were found in the given list");
		System.out.println("Locating images");
		HashMap<Integer, Integer> occurance = new HashMap<Integer, Integer>();
		int lastmax = 0;
		for(var img: imgfiles) {
			int key = mdh.readTotalParts(img);
			lastmax = key;
			if(key > imgfiles.size())
				continue;
			Integer val = occurance.get(key);
			if(val == null) {
				occurance.put(key, 1);
			}else {
				occurance.replace(key, val + 1);
				
			}
		}
		if(occurance.size()==0)
			throw new NullPointerException("no hidden data are stored in these images were");
//		System.out.println("filtering images");
		int totalim = 0;
		if(occurance.size() > 1) {
			var keys = new ArrayList<Integer>(occurance.keySet());
			var values = new ArrayList<Integer>(occurance.values());
			for(int i = 0; i < values.size(); i ++) {
				for(int j = 0; j < values.size() - i - 1; j ++) {
					if(values.get(j) < values.get(j + 1)) {
						int tv = values.get(j+1);
						int tk = keys.get(j+1);
						values.set(j+1, values.get(j));
						values.set(j, tv);
						keys.set(j+1, keys.get(j));
						keys.set(j, tk);
					}
				}
			}
			totalim = values.get(0);
		}
		else 
			totalim = lastmax;
		
		if(totalim > imgfiles.size()) 
			throw new IOException("there are no hidden data in these images, or there might be missing images");
		
		this.imgFiles = new File[totalim];
		for(var im : imgfiles) {
			int index = mdh.readPartIndex(im);
			if(index >= totalim)
				continue;
			if(this.imgFiles[index] != null && mdh.readTotalParts(im) == totalim) {
				if(mdh.readTotalParts(this.imgFiles[index]) == totalim) {
					throw new IOException("There are confilicting images reading the same index: " + this.imgFiles[index].getAbsolutePath() + " and " + im.getAbsolutePath() + "\ncheck your selection and try again");
				}else {
					this.imgFiles[index] = im;
				}
			}else {
				this.imgFiles[index] = im;
			}
		}
		
		for(int i = 0; i < this.imgFiles.length; i++) {
			if(this.imgFiles[i] == null) 
				throw new IOException("the image number "+(i+1)+" is missing, check your selection and try agian");
		}
		
		
		this.outputfolder = new File(outputfolder.getAbsolutePath() + File.separator + "Hidden Files Output" + System.nanoTime());
//		System.out.println("Conditions are met.");
	}
	
	
	
	public void extractData() throws IOException{
		System.out.println("Started Operation: Extraction");
		System.out.println("Setting temp output...");
		File zipFile = File.createTempFile("smth", ".7z");
		FileOutputStream fos = new FileOutputStream(zipFile);
		System.out.println("Extracting Data From images...");
		
		for(int n = 0; n < this.imgFiles.length; n++) {
			System.out.println("Opening img" + n + " ...");
			int filedatalen = mdh.readDataLength(imgFiles[n]);
			int dataoffset = MetaDataHandler.dataIndex.start;
			int reqpix = PixelsHandler.getRequiredPixels(filedatalen + dataoffset, this.bitcount, this.channels);
			Dimension imdim = MetaDataHandler.getImageDimensions(imgFiles[n]);
			Dimension reqdim = PixelsHandler.getRequiredDimension(imdim, reqpix);
			BufferedImage img = PixelsHandler.readSubImage(imgFiles[n], reqdim);
			int[] pix = PixelsHandler.getPixelsArray2(img);
			List<ByteBits> imgDataChunks = new ArrayList<ByteBits>();
			if(this.channels == 4) {
				for(int i = 0; i < reqpix; i++) {
					int p = pix[i];
					imgDataChunks.add(new ByteBits(PixelsHandler.alpha(p), this.bitcount));
					imgDataChunks.add(new ByteBits(PixelsHandler.red(p), this.bitcount));
					imgDataChunks.add(new ByteBits(PixelsHandler.green(p), this.bitcount));
					imgDataChunks.add(new ByteBits(PixelsHandler.blue(p), this.bitcount));
					
				}
			}
			else {
				for(int i = 0; i < reqpix; i++) {
					int p = pix[i];
					imgDataChunks.add(new ByteBits(PixelsHandler.red(p), this.bitcount));
					imgDataChunks.add(new ByteBits(PixelsHandler.green(p), this.bitcount));
					imgDataChunks.add(new ByteBits(PixelsHandler.blue(p), this.bitcount));
				}
			}
			System.out.println("Processing data of img" + n + " ...");
			int value = 0;
			int index = 0;
			int ind = 0;
//			List<Short> storedData = new ArrayList<Short>();
			byte[] data = new byte[imgDataChunks.size() * this.bitcount / 8 + 1];
			for(ByteBits b : imgDataChunks) {
				while(b.hasNext()) {
					value += b.next();
					if(index != 7) //TODO: find a better way to handle this exception
						value <<= 1;
					index++;
					if(index == 8) {
//						storedData.add((short)value);
						data[ind] = (byte) value;
						ind++;
						index = 0;
						value = 0;
					}
				}
			}
			for(var ff: data)
				System.out.print(ff);
			System.out.println();
			System.out.println("Writing data Parts from img" + n+" ...");
//			for(int i = dataoffset; i < filedatalen + dataoffset; i++) {
//				fos.write((byte)(short)storedData.get(i));
//			}
			fos.write(data, dataoffset, filedatalen);
//			storedData = storedData.subList(dataoffset, filedatalen + dataoffset);
//			for(int i = 0; i < storedData.size(); i++) {
//				fos.write((byte)(short)storedData.get(i));
//			}
//			fos.write(storedData.toArray(), offset, datalen);
		}
		fos.close();
		System.out.println("Decompressing Extracted Data...");
		com = new Compressor(zipFile);
		com.decompress(outputfolder);
		zipFile.delete();
		System.out.println("Operation: Extraction, Successful, output folder: " + outputfolder.getAbsolutePath());
	}
	
	//TODO: use arrays instead of ArrayList to increase the speed
	public static ArrayList<Short> extractRawData(BufferedImage img, int bitcount, int channels){
		if(bitcount > 8 || bitcount < 1)
			throw new IndexOutOfBoundsException("bitcount out of bounds(1, 8) : " + bitcount);
		if(channels == 4 && !img.getColorModel().hasAlpha()) 
			throw new UnsupportedOperationException("This image doesn't have Alpha channels");
		var imgChannelsData = new ArrayList<Integer>();
		if(channels == 4)
			for(int i: PixelsHandler.getPixelsArray2(img)) {
				imgChannelsData.add(PixelsHandler.alpha(i));
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
				
			}
		else
			for(int i: PixelsHandler.getPixelsArray2(img)) {
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
			}
		
		ArrayList<ByteBits> imgDataChunks = new ArrayList<>();
		for(int c : imgChannelsData) 
			imgDataChunks.add(new ByteBits(c, bitcount));
		
		int value = 0;
		int index = 0;
		ArrayList<Short> storedData = new ArrayList<>();
		for(ByteBits b : imgDataChunks) {
			while(b.hasNext()) {
				value += b.next();
				if(index != 7) //TODO: find a better way to handle this exception
					value <<= 1;
				index++;
				if(index == 8) {
					storedData.add((short)value);
					index = 0;
					value = 0;
				}
			}
		}
		return storedData;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void extractData2() throws IOException{
		System.out.println("Started Operation: Extraction");
		System.out.println("Setting temp output...");
		File zipFile = File.createTempFile("smth", ".7z");
		FileOutputStream fos = new FileOutputStream(zipFile);
		System.out.println("Extracting Data From images...");
		for(int n = 0; n < this.imgFiles.length; n++) {
			
			System.out.println("Reading Header of img" + n + " ...");
			int filedatalen = mdh.readDataLength(imgFiles[n]);
			int dataoffset = MetaDataHandler.dataIndex.start;
			int reqpix = PixelsHandler.getRequiredPixels(filedatalen + dataoffset, this.bitcount, this.channels);
			
			Dimension imdim = MetaDataHandler.getImageDimensions(imgFiles[n]);
			Dimension reqdim = PixelsHandler.getRequiredDimension(imdim, reqpix);
			
			System.out.println("Opening img" + n + " ...");
			BufferedImage img = PixelsHandler.readSubImage(imgFiles[n], reqdim);
			
			System.out.println("Processing data of img" + n+" ...");
			byte[] zipfiledata = extractData2(img, filedatalen + dataoffset);
			
			System.out.println("Writing data Parts from img" + n+" ...");
			fos.write(zipfiledata, dataoffset, filedatalen);
			fos.close();
			
			System.out.println("Decompressing Extracted Data...");
			com = new Compressor(zipFile);
			com.decompress(outputfolder);
			zipFile.delete();
			
			System.out.println("Operation: Extraction, Successful, output folder: " + outputfolder.getAbsolutePath());		
		}
	}
	
	public byte[] extractData2(BufferedImage img, int read) {
		int value = 0,
			index = 0,
			read_bit_index,
			nextdata,
			write_bit_index = 0;
		byte[] data = new byte[read];
		ChannelsHandler channelshandler = new ChannelsHandler(img, null, this.channels);
		
		while(index < read) {
			read_bit_index = this.bitcount;
			nextdata = channelshandler.next();
			while(--read_bit_index > -1) {
				value += 1 & (nextdata >> read_bit_index);
				value <<= 1;
				write_bit_index++;
			}
			if(write_bit_index >= 8) {
				value >>= 1;
				data[index++] = (byte) (value&0xff);
				value >>= 8;
				write_bit_index = write_bit_index - 8;
			}
		}
		return data;
	}
















	ArrayList<Byte> extractData(File imgfile, int bitcount, boolean usingAlpha) throws IOException{
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
		BufferedImage img = ImageIO.read(imgfile);
		if(usingAlpha && !img.getColorModel().hasAlpha()) 
			throw new UnsupportedOperationException("This image doesn't have Alpha channels");
		var imgChannelsData = new ArrayList<Integer>();
		if(usingAlpha)
			for(int i: PixelsHandler.getPixelsArray2(img)) {
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
				imgChannelsData.add(PixelsHandler.alpha(i));
			}
		else
			for(int i: PixelsHandler.getPixelsArray2(img)) {
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
			}
		for(int i = 0; i < 51;i ++)
			System.out.print(String.format("%03d,", imgChannelsData.get(i)));
//			System.out.print(String.format("%s ", Tools.decimalToBinary(imgChannelsData.get(i), 8)));
		System.out.println();
		ArrayList<ByteBits> imgDataChunks = new ArrayList<>();
		for(int c : imgChannelsData) 
			imgDataChunks.add(new ByteBits(c, bitcount));
//		System.out.println();
		int value = 0;
		int index = 0;
		ArrayList<Short> storedData = new ArrayList<>();
		for(ByteBits b : imgDataChunks) {
			while(b.hasNext()) {
				value += b.next();
				if(index != 7) //TODO: find a better way to handle this exception
					value <<= 1;
				index++;
				if(index == 8) {
					storedData.add((short)value);
					index = 0;
					value = 0;
				}
			}
		}
		long dataLength = 0;
//		for(int i = 0; i < 51; i++) 
//			System.out.print(Tools.decimalToBinary(storedData.get(i), 8) + " ");
//		System.out.println();
		/*get data size */{
			byte[] sizeAsBytes = new byte[8];
			
			for(int i = 0; i < 8; i++) 
				sizeAsBytes[i] = (byte)(short)storedData.get(i);
			dataLength = Tools.bytesToLong(sizeAsBytes);
		}
		System.out.println(dataLength);
		ArrayList<Byte> fileData = new ArrayList<>();
		for(int i = 0; i < dataLength; i++) 
			fileData.add((byte)(short)storedData.get(8 + i));
		return fileData;
	}
	
	
}
