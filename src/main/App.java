package main;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Scanner;

import javax.swing.JFileChooser;

import essentials.Tools;


/**
 * 
 * @author Eboubaker Bekkouche, 'email: gpscrambor.4862500@gmail.com '
 *
 */
public class App{
	static App main;
//	static String resourcesFolder;
	static final String RUNCODE = "JRUN";
	static File _jar, _jardir;
	static final String BATNAME = "runner.bat";
	Scanner scn;
//	static Class<? extends Main> mainClass;
	
	public static void main(String[] args) throws Exception{
			initilizeResources();
//			main.test2();
//			System.exit(1);
			if(args.length == 0 || !args[0].equals("JRUN")) {
				File runner = createRunner();
				Desktop.getDesktop().open(runner);
				System.exit(0);
			}
//			new File(_jardir.getAbsolutePath() + File.separator + BATNAME).delete();
			main.start();
			System.out.println("Execution ended, Press any key to close");
//		float start = System.nanoTime();
//		main.start();
//		float end = System.nanoTime();
//		System.out.println("Time : " + (end-start)/1E9);
	}
	
	public static File createRunner() throws FileNotFoundException, URISyntaxException {
		File bat = new File(_jardir.getAbsolutePath() + File.separator + BATNAME);
		PrintStream p = new PrintStream(bat);
		p.println("@echo off");
		p.println("title Corium By Eboubaker Bekkouche");
		p.println("cd \"" + _jardir + "\"" );
		p.println("java -Dsun.java2d.opengl=true -Dsun.java2d.accthreshold=0 -Xmx3G -Xms200M -jar \"" + _jar.getName() + "\" " + RUNCODE);
		p.println("pause");
		p.println("del runner.bat");
		p.close();
		return bat;
	}
	public void test() throws Exception{
		File[] lisf = new File[] {new File("G:\\message.txt")};
		File[] lism = new File[] {new File("C:\\Users\\MCS\\Desktop\\samples\\485806.jpg")};
		int bitcount = 4;
		int channels = 4;
		Inserter ins = new Inserter(lisf, lism, _jardir, bitcount, channels);
		ins.insertData();
	}
	public void test2() throws Exception{
		File[] lisf = new File[] {new File("G:\\Play\\Factorio.v0.16.51\\factorio-dump-previous.dmp")};
		File[] lism = new File[] {new File("E:\\Programming\\Processing\\IMG_Data_Inserter\\Hidden Data122287297555500")};
		int bitcount = 4;
		int channels = 4;
		Extractor etest = new Extractor(lism , getJarDirectory(), bitcount, channels);
		etest.extractData2();
	}
	public void start() throws IndexOutOfBoundsException, FileNotFoundException, IOException, OutOfImageSpaceException{
		try {
			main.scn = new Scanner(System.in);
			
			System.out.println(">> Do you Want to Insert or Extract Data?, Yes = Insert, No = Extract");
			if(inputBoolean()) {
				System.out.println(">> Insertion Selected");
				insertMode();
			}else {
				System.out.println(">> Extraction Selected");
				extractMode();
			}
			main.scn.close();
		}catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void extractMode() throws Exception {
		System.out.println(">> Choose images or folders containing images to retrieve the data From them (multi selection accepted)");
		File[] lism = Tools.PromptSelectFiles("choose images/folders to hide data in them", null, null, true, JFileChooser.FILES_AND_DIRECTORIES);
		System.out.println(">> insert the bitcount that was used for these images(from 1 to 8), IT MUST BE CORRECT VALUE");
		int bitcount = inputInt();
		System.out.println(">> Do These Images Contain Alpha Channels(4 channels), IT MUST BE CORRECT VALUE");
		int channels = inputBoolean() ? 4 : 3;
		Extractor etest = new Extractor(lism , getJarDirectory(), bitcount, channels);
		etest.extractData();
	}
	public void insertMode() throws Exception{
		System.out.println(">> Choose a file or a list of files/directories to hide in images (multi selection accepted)");
		File[] lisf = Tools.PromptSelectFiles("choose files/folders to insert in images", null, null, true, JFileChooser.FILES_AND_DIRECTORIES);
		System.out.println(">> Choose images or folders containing images to hide data in them (multi selection accepted)");
		File[] lism = Tools.PromptSelectFiles("choose images/folders to hide data in them", null, null, true, JFileChooser.FILES_AND_DIRECTORIES);
		System.out.println(">> insert bitcount usage (from 1 to 8), more bits = more capacity can be inserted into images, but images quality will decrease by higher bitcount, Recommended: 3");
		int bitcount = inputInt();
		System.out.println(">> Do you Want to use Alpha Channels of images? (yes = 4 channels->more capacity | no = 3 channels->less capacity)");
		int channels = inputBoolean() ? 4 : 3;
		Inserter ins = new Inserter(lisf, lism, _jardir, bitcount, channels);
		ins.insertData();
	}
//	public static File getResource(String name) {
//		return new File(resourcesFolder + name);
//	}
	public int inputInt() {
		int ret = 0;
		while(true) {
			System.out.print("-> Input(Num): ");
			String s = scn.nextLine();
			System.out.println();
			try {
				ret = Integer.parseInt(s);
				break;
			}catch (Exception e) {
				System.out.println("-> Bad Input: " + s);
			}
		}
		return ret;
	}
	public boolean inputBoolean() {
		while(true) {
			System.out.print("-> Input (Y/N): ");
			String s = scn.nextLine();
			System.out.println();
			if(s.length() == 1) {
				if(s.toLowerCase().charAt(0) == 'y')
					return true;
				if(s.toLowerCase().charAt(0) == 'n')
					return false;
			}
			System.out.println("-> Bad Input: " + s);
		}
	}
	public File getJarDirectory() throws URISyntaxException {
		return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
	}
	public File getJarFile() throws URISyntaxException {
		return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
	}
	public static void initilizeResources() throws URISyntaxException {
		main = new App();
//		resourcesFolder = main.getClass().getResource("/sources").getFile() + "\\";
		_jar = main.getJarFile();
		_jardir = _jar.getParentFile();
	}
}


