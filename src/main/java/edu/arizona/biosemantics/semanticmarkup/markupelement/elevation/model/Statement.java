package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.StatementAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class Statement extends Element {
	
	@XmlPath("@" + StatementAttribute.text)
	private String text;
	
	@XmlPath("@" + StatementAttribute.id)
	private String id;

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public void removeElementRecursively(Element element) {
		
	}
}
