package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class Resource extends Element  {

	private String name;
	private String version;
	private String type;
	
	public Resource() { }

	public Resource(String name, String version, String type) {
		super();
		this.name = name;
		this.version = version;
		this.type = type;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
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

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}

}

