package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

public class TransformationReport {

	private String charaparserVersion;
	private String glossaryType;
	private String glossaryVersion;
	
	public TransformationReport(String charaparserVersion, String glossaryType,
			String glossaryVersion) {
		this.charaparserVersion = charaparserVersion;
		this.glossaryType = glossaryType;
		this.glossaryVersion = glossaryVersion;
	}
	
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
}
