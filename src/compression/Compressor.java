package compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;


public class Compressor {
	private Progress progress;
	private boolean compressing;
	private boolean decompressing;
	private boolean showProgress;
	private int sleepinterval;
	private File zipfile;
	private Thread tracker = new Thread(new ProgressInfo());
	
	private class ProgressInfo implements Runnable{
		public void run() {
			System.out.println();
			if(compressing) {
				while(compressing) {
					System.out.print("\r");
					System.out.print(String.format("compressing %.2f", 100 * progress.getPercentage())+"%");
					try {
						Thread.sleep(sleepinterval);
					} catch (InterruptedException e) {}
				}
				System.out.print("\r");
				System.out.println("compressing 100.00%");
			}else if(decompressing) {
				while(decompressing) {
					System.out.print("\r");
					System.out.print(String.format("decompressing %.2f", 100 * progress.getPercentage())+"%");
					try {
						Thread.sleep(sleepinterval);
					} catch (InterruptedException e) {}
				}
				System.out.print("\r");
				System.out.println("decompressing 100.00%");
			}
		}
	};
	public Compressor(File zipfile) {
		this.zipfile = zipfile;
	}
	public void setShowProgress(boolean show, int interval) {
		showProgress = show;
		sleepinterval = interval;
	}
	
	public void decompress(File destination) throws IOException {
		decompressing = true;
        SevenZFile sevenZFile = new SevenZFile(zipfile);
        long size = 0;
        for(var entry: sevenZFile.getEntries()) 
        	size += entry.getSize();
        
        sevenZFile = new SevenZFile(zipfile);
        progress = new Progress(size);
        if(showProgress)
        	tracker.start();
        SevenZArchiveEntry entry;
        while ((entry = sevenZFile.getNextEntry()) != null){
            if (entry.isDirectory()){
                continue;
            }
            File curfile = new File(destination, entry.getName());
            File parent = curfile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(curfile);
            byte[] buff = new byte[4096];
            
            long total = entry.getSize();
            long totalread = 0;
            
            while(totalread < total) {
            	totalread += sevenZFile.read(buff);
            	out.write(buff);
            	progress.addProgress(4096);
            }
            int lastread = (int)(total - totalread);
            sevenZFile.read(buff, 0, lastread);
            out.write(buff, 0, lastread);
            progress.addProgress(lastread);
//            byte[] content = new byte[(int) entry.getSize()];
//            sevenZFile.read(content, 0, content.length);
//            out.write(content);
            out.close();
        }
        sevenZFile.close();
        decompressing = false;
        try {
			Thread.sleep(sleepinterval);
		} catch (InterruptedException e) {
		}
	}
	public void compress(File ...files) throws IOException {
		compressing = true;
		var zip = new SevenZOutputFile(zipfile);
		progress = Progress.setForFilesSize(files);
		if(showProgress)
        	tracker.start();
		addToArchive(zip, "", files);
		zip.finish();
		zip.close();
		compressing = false;
		
		try {
			Thread.sleep(sleepinterval);
		} catch (InterruptedException e) {
		}
	}
	private void addToArchive(SevenZOutputFile output, String zipdir, File ...files) throws IOException {
		if(files == null)
			return;
		for(var f: files) {
			if(f.isDirectory()) {
				addToArchive(output, zipdir  + f.getName() + File.separator, f.listFiles());
			}
			else {
				SevenZArchiveEntry entry = output.createArchiveEntry(f, zipdir + f.getName());
				output.putArchiveEntry(entry);
				
				FileInputStream is = new FileInputStream(f);
				byte[] buff = new byte[4096];
				int read;
				while((read = is.read(buff)) >= 0) {
					output.write(buff, 0, read);
					progress.addProgress(read);
				}
				output.closeArchiveEntry();
				is.close();
			}
		}
	}
	
	public Progress getProgress() {
		return progress.copy();
	}
}







class Progress{
	private float total;
	private float current;
	public Progress(float t) {
		total = t; 
	}
	private Progress(float t, float c) {
		total = t; 
		current = c;
	}
	protected Progress copy() {
		return new Progress(total, current);
	}
	public float getPercentage() {
		return current / total;
	}
	protected void addProgress(float count) {
		current += count;
	}
	protected static Progress setForFilesSize(File ...files) {
		return new Progress(getFileTotalSize(files));
	}
	private static float getFileTotalSize(File ...files) {
		float tot = 0;
		if(files == null)
			return 0;
		for(File f: files) {
			if(f.isDirectory())
				tot += getFileTotalSize(f.listFiles());
			else
				tot += f.length();
		}
		return tot;
	}
}
