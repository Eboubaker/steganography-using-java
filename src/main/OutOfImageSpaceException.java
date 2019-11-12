package main;

public class OutOfImageSpaceException extends Exception {
	private static final long serialVersionUID = -3157314172422754947L;
	public OutOfImageSpaceException(long max, long got) {
		
		super(String.format("There is no Space For: %,d Bytes in this/these image(s) ,by using this bitcount this/these image(s) can only hold: %,d Bytes", got, max));
	}
}
