package vacuum.changedamage.equations;

import vacuum.changedamage.equations.element.Element;
import vacuum.changedamage.equations.element.ElementType;
import vacuum.changedamage.equations.element.number.Number;
import vacuum.changedamage.equations.element.operators.Operator;

public class PostfixNotation {
	public double value = 0;
	private Element[] e;

	public PostfixNotation(Element[] e){
		this.e = e;
	}

	public double evaluate(){
		if(e.length == 0)
			return 0;
		
		ElementNode head;
		
		/* Init linked list */
		{
			head = new ElementNode(e[0]);
			ElementNode n = head;

			for (int i = 1; i < e.length; i++) {
				ElementNode next = new ElementNode(e[i]);
				next.prev = n;
				n.next = next;
				n = next;
			}
		}
		/* End linked list */

		/* Init analysis */
		while(head.next != null){
			boolean flag = true;
			ElementNode n = head;
			int numCount = 0;
			while(n != null){
				//traverse list
				if(n.getElement().getType().equals(ElementType.OPERATOR)){
					Operator op = (Operator) n.getElement();
					int operands = op.operands();
					
					//verify that we have enough operands
					if(operands <= numCount){

						/* extract operands */
						ElementNode first = n;
						Number[] numbers = new Number[operands];
						for (int i = operands - 1; i >= 0; i--) {
							first = first.prev;
							numbers[i] = (Number) first.getElement();
						}

						/* get result */
						ElementNode result = new ElementNode(op.evaluate(numbers));

						/* remove elements from list and insert result */
						if(first.prev == null){ //working with head
							head = result;
						} else {
							first.prev.next = result;
							result.prev = first.prev;
						}
						if(n.next != null){
							result.next = n.next;
							n.next.prev = result;
						}

						/* indicate an operation was done */
						flag = false;
						
						/* correct the number of operands available */
						numCount -= operands;
					} else {
						//reset the number of operands to 0
						numCount = 0;
					}
				} else if (n.getElement().getType().equals(ElementType.NUMBER)){
					numCount++;
				}
				n = n.next;
			}
			
			//check that an operation was done during the traversal
			if(flag){
				throw new ArithmeticException("Caught in infinite loop.");
			}
		}
		if(head.getElement().getType().equals(ElementType.OPERATOR))
			throw new ArithmeticException("Final element is an operator.");
		return ((Number)head.getElement()).getValue();
	}
}
