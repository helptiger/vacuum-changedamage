package vacuum.changedamage.equations.element;

public class ElementFormatException extends RuntimeException {
	
	public ElementFormatException(String s){
		super("Illegal element: " + s);
	}
}
