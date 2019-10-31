package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import essentials.Tools;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import test.Test;



public class Main{
	static Main main;
	static String resourcesFolder;
//	static Class<? extends Main> mainClass;
	
	public static void main(String[] args) throws Exception{
		initilizeResources();
		Test.main();
		 
//		main.start();
	}
	
	
	public void start() throws IndexOutOfBoundsException, FileNotFoundException, IOException, OutOfImageSpaceException{
		
		File sample = getResource("sample1.jpg"),
			 data = getResource("bin.7z"),
			 output = getResource("inserted_image.png"),
			 extractedFileOutput = getResource("extracted.rar");
		
		Extractor extractor = new Extractor();
		Inserter inserter = new Inserter();
		
		System.out.println("Started");
		inserter.insertData(sample, data, output, 2, true);
//		BufferedImage img = Pixels.getImageFromArray(new int[]{Pixels.argbtopixel(128, 255, 0, 127)}, 1, 1, BufferedImage.TYPE_INT_ARGB);
//		ImageIO.write(img, "png", extractedFileOutput);
//		BufferedImage img = ImageIO.read(extractedFileOutput);
//		int[] imgpixels = Pixels.getPixelsArray(img);
//		System.out.println(Pixels.alpha(imgpixels[0]));
		
//		Tools.createFileFromData(extractor.extractData(output, 2, true), extractedFileOutput);
		System.out.println("Compleated");
	}
	
	
	
	public static File getResource(String name) {
		return new File(resourcesFolder + name);
	}
	
	
	public static void initilizeResources() {
		main = new Main();
		resourcesFolder = main.getClass().getResource("/sources").getFile() + "\\";
		try {
            SevenZip.initSevenZipFromPlatformJAR();
            System.out.println("7-Zip-JavaBinding was initialized");
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
	}
}


