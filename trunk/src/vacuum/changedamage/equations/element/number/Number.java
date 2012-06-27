package vacuum.changedamage.equations.element.number;

import vacuum.changedamage.equations.element.Element;
import vacuum.changedamage.equations.element.ElementType;

public class Number extends Element {
	
	
	protected double value;

	public Number(double value){
		this.value = value;
	}

	@Override
	public ElementType getType() {
		return ElementType.NUMBER;
	}
	
	public double getValue(){
		return value;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}

}
