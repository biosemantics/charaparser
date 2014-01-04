package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn;

public class AdjectiveReplacementForNoun {

	private String adjective;
	private String noun;
	private String source;
	
	public AdjectiveReplacementForNoun() {
		
	}
	
	public AdjectiveReplacementForNoun(String adjective, String noun,
			String source) {
		super();
		this.adjective = adjective;
		this.noun = noun;
		this.source = source;
	}
	public String getAdjective() {
		return adjective;
	}
	public void setAdjective(String adjective) {
		this.adjective = adjective;
	}
	public String getNoun() {
		return noun;
	}
	public void setNoun(String noun) {
		this.noun = noun;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	

}
