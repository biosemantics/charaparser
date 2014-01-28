package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class Software extends Element {

	private String version;
	private String type;
	private String name;
	
	public Software() { }
	
	public Software(String name, String type, String version) {
		this.name = name;
		this.type = type;
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}

}
