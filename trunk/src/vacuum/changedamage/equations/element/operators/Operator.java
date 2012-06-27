package vacuum.changedamage.equations.element.operators;

import vacuum.changedamage.equations.element.Element;
import vacuum.changedamage.equations.element.ElementType;
import vacuum.changedamage.equations.element.number.Number;

public abstract class Operator extends Element {

	@Override
	public ElementType getType() {
		return ElementType.OPERATOR;
	}
	
	public Number evaluate(Number... n){
		if(n.length != operands())
			throw new IllegalArgumentException("Must have " + operands() + " operands.");
		return eval(n);
	}
	
	protected abstract Number eval(Number[] n);
	
	public abstract int operands();

}
