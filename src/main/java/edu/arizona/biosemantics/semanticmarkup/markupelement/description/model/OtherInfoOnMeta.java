package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class OtherInfoOnMeta extends Element {

	private String value;
	private String type;
	
	public OtherInfoOnMeta() { }
	
	public OtherInfoOnMeta(String value, String type) {
		super();
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}

}

