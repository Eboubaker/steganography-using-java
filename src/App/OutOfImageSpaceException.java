package App;

public class OutOfImageSpaceException extends Exception {
	private static final long serialVersionUID = -3157314172422754947L;
	public OutOfImageSpaceException(int max, long got) {
		super("There is no Space For: " + got + "Bytes in this image ,by using this bitcount this image can only hold: " + max + "Bytes");
	}
}
