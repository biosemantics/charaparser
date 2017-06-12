package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

/**
 * pTerm = term:category
 * @author hongcui
 *
 */
public class CachedHasSynonyms implements KnowsSynonyms {

	private KnowsSynonyms hasSynonyms;
	private Map<String, Set<SynonymSet>> cache = new HashMap<String, Set<SynonymSet>>();

	
	public CachedHasSynonyms(KnowsSynonyms hasSynonyms) {
		this.hasSynonyms = hasSynonyms;
	}

	/*
	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		if(cache.containsKey(term))
			return cache.get(term);
		else {
			Set<SynonymSet> result = hasSynonyms.getSynonyms(term);
			cache.put(term, result);
			return result;
		}
	}*/

	/**
	 * pTerm = term:category
	 */
	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		if(cache.containsKey(term+":"+category))
			return cache.get(term+":"+category);
		else {
			Set<SynonymSet> result = hasSynonyms.getSynonyms(term, category);
			cache.put(term, result);
			return result;
		}
	}
	
}
