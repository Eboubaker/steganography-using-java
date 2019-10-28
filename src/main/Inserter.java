package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import data.ByteBits;

//TODO: 
//use arrays instead of ArrayList to increase the speed
//
public class Inserter {
	private File data;
	private File imgFile;
	private int bitcount;
	private boolean useAlpha;
	
	public File insertData(File imgfile, File data, File output, int bitcount, boolean useAlpha) throws IndexOutOfBoundsException, IOException, OutOfImageSpaceException{
		int imgChannels = useAlpha ? 4 : 3;
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
		BufferedImage img = ImageIO.read(imgfile);
		
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//		if(useAlpha && !img.getColorModel().hasAlpha()) 
//			throw new UnsupportedOperationException("This image doesn't support Alpha channels");
		
		int[] imgpixels = PixelsHandler.getPixelsArray2(img);
		//how much bytes can the image hold:
		long imgContainerSize = imgpixels.length * imgChannels * bitcount / 8;//in bytes
		//the total data that will be inserted into the image
		// + 4 bytes for the length of the file(long)
		long filelength = data.length() + 4;
		System.out.println(imgContainerSize);
		if(filelength > imgContainerSize)
			throw new OutOfImageSpaceException(imgContainerSize, filelength);
		
		var filebytes = new ArrayList<ByteBits>();
		byte[] sizeAsBytes = Tools.longToBytes(data.length());
		//we add the length of the file to the data(so we can know when to stop reading when we are extracting the data)
		for(byte b: sizeAsBytes) 
			filebytes.add(new ByteBits(b));
		try(var is = new FileInputStream(data);){
			for(int i : is.readAllBytes()) 
				filebytes.add(new ByteBits(i));
		}
		var imgChannelsData = new ArrayList<Integer>();
		if(useAlpha)
			for(int i: imgpixels) {
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
				imgChannelsData.add(PixelsHandler.alpha(i));
			}
		else
			for(int i: imgpixels) {
				imgChannelsData.add(PixelsHandler.red(i));
				imgChannelsData.add(PixelsHandler.green(i));
				imgChannelsData.add(PixelsHandler.blue(i));
			}
		for(int i = filebytes.size() / bitcount; i < imgChannelsData.size();i ++)
			imgChannelsData.set(i, ((imgChannelsData.get(i) >> bitcount)<<bitcount) | ((byte)(Math.random() * Tools.fillbits(bitcount))));
//		for(int i = 0; i < 51;i ++)
//			System.out.print(String.format("%03d,", imgChannelsData.get(i) & ((int)Tools.fillbits(8)^(int)Tools.fillbits(bitcount))));
//			System.out.print(String.format("%s ", Tools.decimalToBinary(imgChannelsData.get(i) & ((int)Tools.fillbits(8)^(int)Tools.fillbits(bitcount)), 8)));
//		System.out.println();
		{
			int index = 0;
			int newval = imgChannelsData.get(index) >> bitcount;
			int i = 0;
			int insert_index = 0;
			for(ByteBits b : filebytes) {
				while(b.hasNext()) {
					newval <<= 1;
					newval += b.next();
					i++;
					if(i == bitcount) {
						imgChannelsData.set(index, newval);
						index++;
//						if(index % imgChannels == 0) {
//							int ind = (index-1) / imgChannels;
//							int x = ind % w;
//							int y = ind / w;
//							if(useAlpha)
//								img.setRGB(x, y, PixelsHandler.argbtopixel(imgChannelsData.get(index - 1), imgChannelsData.get(index - 4), imgChannelsData.get(index - 3), imgChannelsData.get(index-2)));
//							else
//								img.setRGB(x, y, PixelsHandler.rgbtopixel(imgChannelsData.get(index - 3), imgChannelsData.get(index - 2), imgChannelsData.get(index-1)));
//						}
							
						newval = imgChannelsData.get(index) >> bitcount;
						i = 0;
					}
				}
			}
		}
//		var outs = ImageIO.createImageOutputStream(output);
//		for(int i : imgChannelsData) {
//			outs.write(i);
//		}
		for(int i = 0; i < 153;i ++)
			System.out.print(String.format("%03d,", imgChannelsData.get(i)));
//			System.out.print(String.format("%s ", Tools.decimalToBinary(imgChannelsData.get(i), 8)));
		System.out.println();
//		
		int[] newpixels = new int[img.getHeight() * img.getWidth()];
		int max = img.getWidth() * img.getHeight() * imgChannels;
		if(useAlpha){
			for(int i = 0;i < max; i += 4) {
//				System.out.print(String.format("%03d,", imgChannelsData.get(i/4)));
				newpixels[i / 4] = PixelsHandler.argbtopixel(imgChannelsData.get(i+3), imgChannelsData.get(i), imgChannelsData.get(i+1), imgChannelsData.get(i+2));
			}
			System.out.println();
		}else {
			for(int i = 0;i < max; i += 3) {
					newpixels[i / 3] = PixelsHandler.rgbtopixel(imgChannelsData.get(i), imgChannelsData.get(i+1), imgChannelsData.get(i+2));
			}
		}
//		
//		for(int i = 0; i < 52; i++){
////				System.out.print(String.format("%03d,", Pixels.red(newpixels[i])));
////				System.out.print(String.format("%03d,", Pixels.green(newpixels[i])));
////				System.out.print(String.format("%03d,", Pixels.blue(newpixels[i])));
////				System.out.print(String.format("%03d,", Pixels.alpha(newpixels[i])));
//				System.out.print(String.format("%s ", Tools.decimalToBinary(PixelsHandler.red(newpixels[i]), 8)));
//				System.out.print(String.format("%s ", Tools.decimalToBinary(PixelsHandler.green(newpixels[i]), 8)));
//				System.out.print(String.format("%s ", Tools.decimalToBinary(PixelsHandler.blue(newpixels[i]), 8)));
//				System.out.print(String.format("%s ", Tools.decimalToBinary(PixelsHandler.alpha(newpixels[i]), 8)));
//			}
//		System.out.println();
//		//TODO: only process the changed values
		
		for(int i = 0; i < newpixels.length; i++) {
			int x = i % w;
			int y = i / w;
			newimg.setRGB(x, y, newpixels[i]);
		}
		for(int i = 0; i < 153;i ++)
			System.out.print(String.format("%03d,", newpixels[i]));
//			System.out.print(String.format("%s ", Tools.decimalToBinary(imgChannelsData.get(i), 8)));
		System.out.println();
		{
			
			String imgname = imgfile.getName();
//			String ext = imgname.substring(imgname.lastIndexOf('.') + 1);
//			saveImageAsJPEG(img, new FileOutputStream(output), 100);
			ImageIO.write(newimg, "png", output);
		}
		return output;
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
