package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

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
		
		return null;
	}

}
