package vacuum.changedamage.equations.element;

public class ElementFormatException extends RuntimeException {
	
	private static final long serialVersionUID = -6289476362327993062L;

	public ElementFormatException(String s){
		super("Illegal element: " + s);
	}
}
