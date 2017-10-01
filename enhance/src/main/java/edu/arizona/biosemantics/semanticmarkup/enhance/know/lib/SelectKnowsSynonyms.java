package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

/*
 * this class decides which knowsSynonyms (CVS or ontology, or both) to use
 * 
 */
public class SelectKnowsSynonyms implements KnowsSynonyms {

	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		// TODO Auto-generated method stub
		
		//if ontology exist, check it first, then check cvs
		//else check cvs
		return null;
	}

}
