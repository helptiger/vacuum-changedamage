package vacuum.changedamage.equations.element.operators;

import vacuum.changedamage.equations.element.number.Number;

public class LeftShift extends Operator {

	@Override
	protected Number eval(Number[] n) {
		return new Number(((int)n[0].getValue()) << ((int)n[1].getValue()));
	}

	@Override
	public int operands() {
		return 2;
	}

	@Override
	public String toString() {
		return "<<";
	}

}
