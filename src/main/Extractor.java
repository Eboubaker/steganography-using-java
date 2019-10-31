package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import data.ByteBits;
import essentials.PixelsHandler;
import essentials.Tools;

public class Extractor {
	//TODO: use arrays instead of ArrayList to increase the speed
	public static ArrayList<Short> extractRawData(BufferedImage img, int bitcount, boolean usingAlpha){
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
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
