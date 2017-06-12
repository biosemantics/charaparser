package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public interface KnowsSynonyms {
	
	public static class SynonymSet implements Iterable<String>{
		
		private String preferredTerm;
		private String category;
		private Set<String> synonyms = new HashSet<String>(); //synonyms of the concept
		
		public SynonymSet(String term, String category, Set<String> synonyms) {
			this.preferredTerm = term;
			this.category = category;
			this.synonyms = synonyms;
		}

		public String getPreferredTerm() {
			return preferredTerm;
		}

		public Set<String> getSynonyms() {
			return synonyms;
		}
		
		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}
		
		@Override
		public Iterator<String> iterator() {
			List<String> terms = new LinkedList<String>();
			terms.add(preferredTerm);
			terms.addAll(synonyms);
			return terms.iterator();
		}
		
	}
	/**
	 * may return an empty set
	 * @param term
	 * @param category
	 * @return
	 */
	public Set<SynonymSet> getSynonyms(String term, String category);
	
}
