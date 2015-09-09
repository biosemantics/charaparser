package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.util.HashSet;
import java.util.Set;

public interface HasSynonyms {
	
	public static class SynonymSet {
		
		private String preferredTerm;
		private Set<String> synonyms = new HashSet<String>();
		
		public SynonymSet(String preferredTerm, Set<String> synonyms) {
			this.preferredTerm = preferredTerm;
			this.synonyms = synonyms;
		}

		public String getPreferredTerm() {
			return preferredTerm;
		}

		public Set<String> getSynonyms() {
			return synonyms;
		}
		
	}
	
	public Set<SynonymSet> getSynonyms(String term);
	
}
