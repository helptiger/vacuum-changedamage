package vacuum.changedamage.equations;

import java.util.HashMap;

import vacuum.changedamage.equations.element.Element;
import vacuum.changedamage.equations.element.ElementFormatException;
import vacuum.changedamage.equations.element.number.Number;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.equations.element.number.VariablePool;
import vacuum.changedamage.equations.element.operators.Absolute;
import vacuum.changedamage.equations.element.operators.Addition;
import vacuum.changedamage.equations.element.operators.Ceiling;
import vacuum.changedamage.equations.element.operators.Division;
import vacuum.changedamage.equations.element.operators.Floor;
import vacuum.changedamage.equations.element.operators.Multiplication;
import vacuum.changedamage.equations.element.operators.Random;
import vacuum.changedamage.equations.element.operators.Round;
import vacuum.changedamage.equations.element.operators.Subtraction;

public class ExpressionParser {
	public static PostfixNotation parsePostfix(String eq, VariablePool pool){
		String[] parts = eq.toLowerCase().split(" ");
		Element[] elements = new Element[parts.length];
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			
			/* variables */
			Variable v = pool.getVariable(part);
			if(v != null){
				elements[i] = v;
				continue;
			}
			
			/* numbers */
			try{
				elements[i] = new Number(Double.parseDouble(part));
				continue;
			} catch (NumberFormatException ex){
				//ignore
			}
			
			/* operators */
			try {
				Class clazz = stringToOperation.get(part);
				if(clazz != null){
					Element e = (Element) clazz.newInstance();
					if(e != null){
						elements[i] = e;
						continue;
					}
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			throw new ElementFormatException(part);
		}
		return new PostfixNotation(elements);
	}

	private static HashMap<String, Class> stringToOperation = new HashMap<String, Class>();

	static
	{
		stringToOperation.put("+", Addition.class);
		stringToOperation.put("abs", Absolute.class);
		stringToOperation.put("-", Subtraction.class);
		stringToOperation.put("*", Multiplication.class);
		stringToOperation.put("x", Multiplication.class);
		stringToOperation.put("/", Division.class);
		stringToOperation.put("floor", Floor.class);
		stringToOperation.put("fl", Floor.class);
		stringToOperation.put("round", Round.class);
		stringToOperation.put("rd", Round.class);
		stringToOperation.put("ceiling", Ceiling.class);
		stringToOperation.put("ceil", Ceiling.class);
		stringToOperation.put("rand", Random.class);
		stringToOperation.put("random", Random.class);
		stringToOperation.put("r", Random.class);
	}
}
