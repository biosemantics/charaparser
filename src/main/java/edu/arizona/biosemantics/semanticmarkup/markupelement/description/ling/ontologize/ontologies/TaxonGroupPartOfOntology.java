package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.ontologies;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;

/**
 * ontologies containing part_of relationships
 * @author updates
 *
 */
public class TaxonGroupPartOfOntology {

		public static Ontology getOntologies(TaxonGroup taxonGroup) {
			switch(taxonGroup) {
			case ALGAE:
				return null;
			case CNIDARIA:
				return null;
			case FOSSIL:
				return null;
			case GASTROPODS:
				return null;
			case HYMENOPTERA:
				return Ontology.HAO;
			case PLANT:
				return Ontology.PO;
			case PORIFERA:
				return Ontology.PORO;
			}
			return null;
		}	
	

}
