package data;

public class ByteBits{
	public short b;
	private int index = 7;
	public ByteBits(int i) {
		b = (short) i;
	}
	public ByteBits(int i, int bitcount) {
		index = bitcount - 1;
		b = (short) i;
	}
	public boolean hasNext() {
		return index > -1;
	}
	/**
	 * reads bits from left to right.
	 * 
	 * @return the next bit in this byte.
	 */
	public short next() {
		int n = (b&(1<<index))>>index;
		index --;
		return (short)n;
	}
}
