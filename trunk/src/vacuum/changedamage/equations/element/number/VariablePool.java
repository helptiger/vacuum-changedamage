package vacuum.changedamage.equations.element.number;

import java.util.HashMap;

public class VariablePool {
	
	public boolean autoRegister;

	private HashMap<String, Variable> variables;

	public VariablePool(boolean autoRegister){
		variables = new HashMap<String, Variable>();
		this.autoRegister = autoRegister;
	}

	public Variable register(String name, double value){
		String nameLC = name.toLowerCase();
		if(variables.containsKey(nameLC)){
			Variable v = getVariable(nameLC);
			v.setValue(value);
			return v;
		}
		Variable v = new Variable(value, name);
		variables.put(nameLC, v);
		return v;
	}

	public boolean unregister(String name){
		return variables.remove(name.toLowerCase()) != null;
	}

	public Variable getVariable(String name){
		String nameLC = name.toLowerCase();
		Variable v = variables.get(nameLC);
		if(v != null)
			return v;
		return autoRegister ? variables.put(nameLC, new Variable(0, name)) : null;
	}
}