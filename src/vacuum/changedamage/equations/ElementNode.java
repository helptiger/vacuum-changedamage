package vacuum.changedamage.equations;

import vacuum.changedamage.equations.element.Element;

public class ElementNode {
	public ElementNode next;
	public ElementNode prev;
	private Element value;
	public Element getElement(){
		return value;
	}
	
	public ElementNode(Element value){
		this.value = value;
	}
}
