package semanticMarkup.markupElement.description.model;


import org.eclipse.persistence.oxm.annotations.XmlPath;

import semanticMarkup.model.Element;

public class Meta extends Element {
	
	private String charaparserVersion;
	private String glossaryType;
	private String glossaryVersion;
	
	public String getCharaparserVersion() {
		return charaparserVersion;
	}
	public void setCharaparserVersion(String charaparserVersion) {
		this.charaparserVersion = charaparserVersion;
	}
	public String getGlossaryType() {
		return glossaryType;
	}
	public void setGlossaryType(String glossaryType) {
		this.glossaryType = glossaryType;
	}
	public String getGlossaryVersion() {
		return glossaryVersion;
	}
	public void setGlossaryVersion(String glossaryVersion) {
		this.glossaryVersion = glossaryVersion;
	}
	@Override
	public void removeElementRecursively(Element element) {
		return;
	}
}
