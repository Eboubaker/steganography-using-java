package App;

public class Byte{
	private short b;
	byte index = 8;
	public Byte(int i) {
		var t = new Byte(i, true);
		String r = "";
		while(t.hasNext())
			r += t.next();
		i = Integer.parseInt(r, 2);
		b = (short) i;
	}
	public Byte(int i, boolean ishelper) {
		b = (short) i;
	}
	public boolean hasNext() {
		return index > 0;
	}
	public short next() {
		index --;
		short n = (short) (b&1);
		b >>= 1;
		return n;
	}
}
