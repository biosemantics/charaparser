package semanticMarkup.eval.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

public class Element {

	@XmlPath("@name")
	private String name;
	
	public Element() { }

	public Element(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
