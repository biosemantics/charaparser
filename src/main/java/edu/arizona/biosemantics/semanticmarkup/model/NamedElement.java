package edu.arizona.biosemantics.semanticmarkup.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

public abstract class NamedElement extends Element {
	
	@XmlPath("@name")
	protected String name;
	
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
