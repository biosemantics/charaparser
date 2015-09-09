package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CachedHasSynonyms implements HasSynonyms {

	private HasSynonyms hasSynonyms;
	private Map<String, Set<SynonymSet>> cache = new HashMap<String, Set<SynonymSet>>();

	public CachedHasSynonyms(HasSynonyms hasSynonyms) {
		this.hasSynonyms = hasSynonyms;
	}

	@Override
	public Set<SynonymSet> getSynonyms(String term) {
		if(cache.containsKey(term))
			return cache.get(term);
		else {
			Set<SynonymSet> result = hasSynonyms.getSynonyms(term);
			cache.put(term, result);
			return result;
		}
	}
	
}
