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
//	public short next() {
//		short n = (short) ((b&0b1000_0000) >> 7);
//		n &= 0b0111_1111;
//		b <<= 1;
//		index --;
//		return n;
//	}
	public short next() {
		int n = (b&(1<<index))>>index;
		index --;
		return (short)n;
	}
}
