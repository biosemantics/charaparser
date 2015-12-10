package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public interface KnowsSynonyms {
	
	public static class SynonymSet implements Iterable<String> {
		
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

		@Override
		public Iterator<String> iterator() {
			List<String> terms = new LinkedList<String>();
			terms.add(preferredTerm);
			terms.addAll(synonyms);
			return terms.iterator();
		}
		
	}
	
	public Set<SynonymSet> getSynonyms(String term);
	
}
