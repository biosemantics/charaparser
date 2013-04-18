package semanticMarkup.know.net;

import java.util.List;

import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;

public class OTOGlossary {

	private List<TermCategory> termCategories;
	private List<TermSynonym> termSynonyms;
	
	public OTOGlossary(List<TermCategory> termCategories, List<TermSynonym> termSynonyms) {
		this.termCategories = termCategories;
		this.termSynonyms = termSynonyms;
	}

	public List<TermCategory> getTermCategories() {
		return termCategories;
	}

	public void setTermCategories(List<TermCategory> termCategories) {
		this.termCategories = termCategories;
	}

	public List<TermSynonym> getTermSynonyms() {
		return termSynonyms;
	}

	public void setTermSynonyms(List<TermSynonym> termSynonyms) {
		this.termSynonyms = termSynonyms;
	}
	
	
	
}
