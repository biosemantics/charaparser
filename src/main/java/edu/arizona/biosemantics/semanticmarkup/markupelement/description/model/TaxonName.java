/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.TaxonNameAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
/**
 * @author Hong Cui
 *
 */
public class TaxonName extends Element{
	
	@XmlPath("@text")
	private String text;
	@XmlPath("@" + TaxonNameAttribute.rank)
	private String rank;
	@XmlPath("@" + TaxonNameAttribute.authority)
	private String authority;
	@XmlPath("@" + TaxonNameAttribute.date)
	private String date;
	
	public TaxonName() {
	}
	public TaxonName(String taxonName) {
		this.text=taxonName;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	@Override
	public void removeElementRecursively(Element element) {
		return;		
	}
	
	
}
