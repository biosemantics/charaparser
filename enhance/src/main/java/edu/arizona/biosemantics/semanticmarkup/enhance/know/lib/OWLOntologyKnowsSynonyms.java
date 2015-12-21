package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class OWLOntologyKnowsSynonyms implements KnowsSynonyms {

	private IInflector inflector;

	public OWLOntologyKnowsSynonyms(IInflector inflector) {
		this.inflector = inflector;
	}
	
	@Override
	public Set<SynonymSet> getSynonyms(String term) {
		Set<SynonymSet> result = new HashSet<SynonymSet>();
		SynonymSet synonymSet = new SynonymSet(term, new HashSet<String>());
		result.add(synonymSet);
		return result;
	}

}
