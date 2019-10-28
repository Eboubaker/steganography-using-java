package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;



public class Data_Inserter{
	public static void main(String[] args) throws Exception{
		 try {
	            SevenZip.initSevenZipFromPlatformJAR();
	            System.out.println("7-Zip-JBinding library was initialized");
	        } catch (SevenZipNativeInitializationException e) {
	            e.printStackTrace();
	        }
//		new Data_Inserter().start();
	}
	public void start() throws IndexOutOfBoundsException, FileNotFoundException, IOException, OutOfImageSpaceException{
		File sample = new File("E:\\Programming\\Processing\\clone\\src\\sample2.png");
		File data = new File("E:\\Programming\\Processing\\clone\\src\\bin.7z");
		File output = new File("E:\\Programming\\Processing\\clone\\src\\inserted_promise.png");
		File extractedFileOutput = new File("E:\\Programming\\Processing\\clone\\src\\extracted.rar");
		Extractor extractor = new Extractor();
		Inserter inserter = new Inserter();
		System.out.println("Started");
//		System.out.println(new String("hello".getBytes()));
		inserter.insertData(sample, data, output, 2, true);
//		BufferedImage img = Pixels.getImageFromArray(new int[]{Pixels.argbtopixel(128, 255, 0, 127)}, 1, 1, BufferedImage.TYPE_INT_ARGB);
//		ImageIO.write(img, "png", extractedFileOutput);
//		BufferedImage img = ImageIO.read(extractedFileOutput);
//		int[] imgpixels = Pixels.getPixelsArray(img);
//		System.out.println(Pixels.alpha(imgpixels[0]));
		
		Tools.createFileFromData(extractor.extractData(output, 2, true), extractedFileOutput);
		System.out.println("Compleated");
	}
}


