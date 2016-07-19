package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public class CachedHasSynonyms implements KnowsSynonyms {

	private KnowsSynonyms hasSynonyms;
	private Map<String, Set<SynonymSet>> cache = new HashMap<String, Set<SynonymSet>>();

	public CachedHasSynonyms(KnowsSynonyms hasSynonyms) {
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
