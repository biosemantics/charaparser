/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.StatementAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 *
 */
public class Value extends Element {
	
	@XmlPath("@" + StatementAttribute.text)
	private String text;
	
	public Value(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.model.Element#removeElementRecursively(edu.arizona.biosemantics.semanticmarkup.model.Element)
	 */
	@Override
	public void removeElementRecursively(Element element) {
		

	}

}
