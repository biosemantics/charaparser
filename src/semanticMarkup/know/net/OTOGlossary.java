package semanticMarkup.know.net;

import java.util.List;

import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;
import edu.arizona.sirls.beans.WordRole;

public class OTOGlossary {

	private List<TermCategory> termCategories;
	private List<TermSynonym> termSynonyms;
	private List<WordRole> wordRoles;

	public OTOGlossary(List<TermCategory> termCategories,
			List<TermSynonym> termSynonyms, List<WordRole> wordRoles) {
		this.termCategories = termCategories;
		this.termSynonyms = termSynonyms;
		this.wordRoles = wordRoles;
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

	public List<WordRole> getWordRoles() {
		return wordRoles;
	}

	public void setWordRoles(List<WordRole> wordRoles) {
		this.wordRoles = wordRoles;
	}
	
}
