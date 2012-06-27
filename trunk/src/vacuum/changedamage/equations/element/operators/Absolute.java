package vacuum.changedamage.equations.element.operators;

import vacuum.changedamage.equations.element.number.Number;

public class Absolute extends Operator{

	@Override
	protected Number eval(Number[] n) {
		return new Number(Math.abs(n[0].getValue()));
	}

	@Override
	public int operands() {
		return 1;
	}
	
	@Override
	public String toString(){
		return "abs";
	}

}
