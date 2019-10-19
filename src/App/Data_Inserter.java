package App;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;



public class Data_Inserter{
	public static void main(String[] args) throws Exception{
		
		new Data_Inserter().start();
	}
	public void start() throws IndexOutOfBoundsException, FileNotFoundException, IOException, OutOfImageSpaceException{
		File sample = new File("E:\\Programming\\Processing\\clone\\src\\cat.png");
		File data = new File("E:\\Programming\\Processing\\clone\\src\\datatest.txt");
		File output = new File("E:\\Programming\\Processing\\clone\\src\\cat2.png");
		System.out.println("Started");
		//insertData(sample, data, output, 3, true);
		for(byte b: extractData(output, 3, true))
			System.out.println(b);
		System.out.println("Compleated");
	}

	public File insertData(File imgfile, File data, File output, int bitcount, boolean useAlpha) throws IndexOutOfBoundsException, FileNotFoundException, IOException, OutOfImageSpaceException{
		
		int imgChannels = useAlpha ? 4 : 3;
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
		//int trimmer = 255 - (int)Math.pow(2,bitcount)+1;
		BufferedImage img = ImageIO.read(imgfile);
		int[] imgpixels = Pixels.getPixelsArray(img);
		if(imgpixels.length * imgChannels * bitcount / 8 < data.length() + 4)
			throw new OutOfImageSpaceException(imgpixels.length * imgChannels * bitcount / 8, data.length() + 4);
		var filebytes = new ArrayList<App.Byte>();
		byte[] sizeAsBytes = Tools.longToBytes(data.length());
		for(byte b: sizeAsBytes) 
			filebytes.add(new Byte(b));
		try(var is = new FileInputStream(data);){
			for(int i : is.readAllBytes()) 
				filebytes.add(new App.Byte(i));
		}
		var imgChannelsData = new ArrayList<Integer>();
		if(useAlpha)
			for(int i: imgpixels) {
				imgChannelsData.add(Pixels.red(i));
				imgChannelsData.add(Pixels.green(i));
				imgChannelsData.add(Pixels.blue(i));
				imgChannelsData.add(Pixels.alpha(i));
			}
		else
			for(int i: imgpixels) {
				imgChannelsData.add(Pixels.red(i));
				imgChannelsData.add(Pixels.green(i));
				imgChannelsData.add(Pixels.blue(i));
			}
		{
			int index = 0;
			int newval = imgChannelsData.get(index) >> bitcount;
			int i = 0;
			for(Byte b : filebytes) {
				while(b.hasNext()) {
					newval <<= 1;
					newval += b.next();
					i++;
					if(i == bitcount) {
						imgChannelsData.set(index, newval);
						index++;
						newval = imgChannelsData.get(index) >> bitcount;
						i = 0;
					}
				}
			}
		}
		int[] newpixels = new int[img.getHeight() * img.getWidth()];
		if(useAlpha){
			int index = 0;
			for(int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					newpixels[index/4] = Pixels.argbtopixel(imgChannelsData.get(index+3), imgChannelsData.get(index), imgChannelsData.get(index+1), imgChannelsData.get(index+2));
					index+=4;
				}
			}
		}else {
			int index = 0;
			for(int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					newpixels[index/3] = Pixels.rgbtopixel(imgChannelsData.get(index), imgChannelsData.get(index+1), imgChannelsData.get(index+2));
					index+=3;
				}
			}
		}
		{
			String imgname = imgfile.getName();
			String ext = imgname.substring(imgname.lastIndexOf('.') + 1);
			BufferedImage newimg = Pixels.getImageFromArray(newpixels, img.getWidth(), img.getHeight());
			ImageIO.write(newimg, ext, output);
		}
		return output;
	}


	ArrayList<java.lang.Byte> extractData(File imgfile, int bitcount, boolean usingAlpha) throws IOException{
		if(bitcount > 8)
			throw new IndexOutOfBoundsException(bitcount);
		BufferedImage img = ImageIO.read(imgfile);
		if(usingAlpha && !img.getColorModel().hasAlpha()) 
			throw new UnsupportedOperationException("This image doesn't have Alpha channels");
		int[] imgPixels = Pixels.getPixelsArray(img);
		ArrayList<Integer> imgChannelsData = new ArrayList<>();
		if(usingAlpha)
			for(int i: imgPixels) {
				imgChannelsData.add(Pixels.red(i));
				imgChannelsData.add(Pixels.green(i));
				imgChannelsData.add(Pixels.blue(i));
				imgChannelsData.add(Pixels.alpha(i));
			}
		else
			for(int i: imgPixels) {
				imgChannelsData.add(Pixels.red(i));
				imgChannelsData.add(Pixels.green(i));
				imgChannelsData.add(Pixels.blue(i));
			}
		ArrayList<App.Byte> imgDataChunks = new ArrayList<>();
		int trimmer = (int)Math.pow(2,bitcount)-1;
		for(int c : imgChannelsData) 
			imgDataChunks.add(new App.Byte(c & trimmer));
		int value = 0;
		int index = 0;
		ArrayList<java.lang.Byte> storedData = new ArrayList<>();
		for(App.Byte b : imgDataChunks) {
			while(b.hasNext()) {
				value += b.next() == 1 ? 1 : 0;
				value <<= 1;
				index++;
				if(index == 8) {
					storedData.add((byte)value);
					index = 0;
					value = 0;
				}
			}
		}
		long dataLength = 0;
		/*get data size */{
			byte[] sizeAsBytes = new byte[8];
			for(int i = 0; i < 8; i++) 
				sizeAsBytes[i] = storedData.get(i);
			dataLength = Tools.bytesToLong(sizeAsBytes);
		}
		System.out.println(dataLength);
		ArrayList<java.lang.Byte> fileData = new ArrayList<>();
		for(int i = 0; i < dataLength; i++) 
			fileData.add(storedData.get(8 + i));
		return fileData;
	}
}


