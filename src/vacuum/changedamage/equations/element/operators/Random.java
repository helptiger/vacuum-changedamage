package vacuum.changedamage.equations.element.operators;

import vacuum.changedamage.equations.element.number.Number;

public class Random extends Operator {

	@Override
	protected Number eval(Number[] n) {
		return new Number(Math.random());
	}

	@Override
	public int operands() {
		return 0;
	}

	@Override
	public String toString() {
		return "random";
	}

}
