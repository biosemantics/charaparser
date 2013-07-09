package semanticMarkup.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

public class NamedElement extends Element {
	
	@XmlPath("@name")
	private String name;
	
	public NamedElement() { }

	public NamedElement(String name) {
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
