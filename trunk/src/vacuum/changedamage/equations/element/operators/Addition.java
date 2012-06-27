package vacuum.changedamage.equations.element.operators;

import vacuum.changedamage.equations.element.number.Number;

public class Addition extends Operator{

	@Override
	protected Number eval(Number[] n) {
		return new Number(n[0].getValue() + n[1].getValue());
	}

	@Override
	public int operands() {
		return 2;
	}
	
	public String toString(){
		return "+";
	}

}
