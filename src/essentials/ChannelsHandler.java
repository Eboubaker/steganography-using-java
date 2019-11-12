package essentials;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class ChannelsHandler{
	BufferedImage img;
	BufferedImage outimg;
	int w;
	int channels;
	Point currentPoint;
	long index;
	long total;
	int currentReadPixel;
	int currentOutPixel;
	int channelIndex;
	public ChannelsHandler(BufferedImage img, BufferedImage outimg, int channels) {
		this.img = img;
		w = img.getWidth();
		total = w * img.getHeight();
		this.channels = channels;
		index = 0;
		currentOutPixel = 0;
		channelIndex = this.channels;
		this.outimg = outimg;
	}
	public boolean hasNext() {
		return index < total && currentReadPixel != channels;
	}
	public int next() {
		int val;
		if(channelIndex == channels) {
			Point p = indexToPoint(true);
			currentReadPixel = img.getRGB(p.x, p.y);
			index++;
			channelIndex = 0;
		}
		if(channelIndex == 0) {
			val =  PixelsHandler.alpha(currentReadPixel);
		}else if(channelIndex == 1) {
			val =  PixelsHandler.red(currentReadPixel);
		}else if(channelIndex == 2) {
			val = PixelsHandler.green(currentReadPixel);
		}else if(channelIndex == 3) {
			val =  PixelsHandler.blue(currentReadPixel);
		}else {
			throw new IndexOutOfBoundsException(channelIndex);
		}
		channelIndex++;
		return val;
	}
	private void set() {
		//TODO:REMOVE
		if(channelIndex != channels)
			throw new IndexOutOfBoundsException();
		Point p = indexToPoint(false);
		outimg.setRGB(p.x, p.y, currentOutPixel);
		currentOutPixel = 0;
	}
	public void add(int nextChannel) {
		currentOutPixel |= nextChannel & 0xff;//TODO: REMOVE & 0xff
		if(channelIndex != channels)
			currentOutPixel <<= 8;
		else
			set();
	}
	private Point indexToPoint(boolean get) {
		Point ret = new Point();
		long ind = get ? index : index - 1;
		long x = ind % w;
		long y = ind / w;
		ret.setLocation(x, y);
		return ret;
	}
	public void reset() {
		index = 0;
		currentReadPixel = img.getRGB(0, 0);
		currentOutPixel = 0;
		channelIndex = channels;
	}
}
