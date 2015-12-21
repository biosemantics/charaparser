package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class OWLOntologyKnowsPartOf implements KnowsPartOf {

	private IInflector inflector;

	public OWLOntologyKnowsPartOf(String ontology, IInflector inflector) {
		this.inflector = inflector;
	}

	@Override
	public boolean isPartOf(String part, String parent) {
		// TODO Auto-generated method stub
		return false;
	}

}
