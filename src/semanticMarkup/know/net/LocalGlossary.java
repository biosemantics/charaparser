package semanticMarkup.know.net;

import java.util.List;

import edu.arizona.sirls.beans.Sentence;
import edu.arizona.sirls.beans.TermCategory;

public class LocalGlossary {

	private List<Sentence> sentences;
	private List<TermCategory> termCategories;
	
	public LocalGlossary(List<Sentence> sentences, List<TermCategory> termCategories) {
		this.sentences = sentences;
		this.termCategories = termCategories;
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public List<TermCategory> getTermCategories() {
		return termCategories;
	}

	public void setTermCategories(List<TermCategory> termCategories) {
		this.termCategories = termCategories;
	}
	
	
	

}
