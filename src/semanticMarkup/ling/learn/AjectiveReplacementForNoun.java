package semanticMarkup.ling.learn;

public class AjectiveReplacementForNoun {

	private String adjective;
	private String noun;
	private String source;
	
	public AjectiveReplacementForNoun() {
		
	}
	
	public AjectiveReplacementForNoun(String adjective, String noun,
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
