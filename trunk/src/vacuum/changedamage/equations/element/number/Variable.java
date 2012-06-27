package vacuum.changedamage.equations.element.number;


public class Variable extends Number{

	/* friendly */ Variable(double value, String name) {
		super(value);
	}
	
	public void setValue(double value){
		this.value = value;
	}
	
}
