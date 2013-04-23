package semanticMarkup.know.net;

import java.util.List;

import edu.arizona.sirls.beans.Term;

public class LocalGlossary {

	private List<Term> structures;
	private List<Term> characters;
	private List<Term> otherTerms;
	
	public LocalGlossary(List<Term> structures, List<Term> characters,	List<Term> otherTerms) {
		this.structures = structures;
		this.characters = characters;
		this.otherTerms = otherTerms;
	}

	public List<Term> getStructures() {
		return structures;
	}

	public void setStructures(List<Term> structures) {
		this.structures = structures;
	}

	public List<Term> getCharacters() {
		return characters;
	}

	public void setCharacters(List<Term> characters) {
		this.characters = characters;
	}

	public List<Term> getOtherTerms() {
		return otherTerms;
	}

	public void setOtherTerms(List<Term> otherTerms) {
		this.otherTerms = otherTerms;
	}

}
