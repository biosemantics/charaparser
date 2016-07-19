package edu.arizona.biosemantics.semanticmarkup.enhance.know.partof;

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

		public static Set<Ontology> getOntologies(TaxonGroup taxonGroup) {
			switch(taxonGroup) {
			case ALGAE:
				return new HashSet<Ontology>();
			case CNIDARIA:
				return new HashSet<Ontology>();
			case FOSSIL:
				return new HashSet<Ontology>();
			case GASTROPODS:
				return new HashSet<Ontology>();
			case HYMENOPTERA:
				return new HashSet<Ontology>(Arrays.asList(new Ontology[] { Ontology.HAO }));
			case PLANT:
				return new HashSet<Ontology>(Arrays.asList(new Ontology[] { Ontology.PO }));
			case PORIFERA:
				return new HashSet<Ontology>(Arrays.asList(new Ontology[] { Ontology.PORO }));
			}
			return new HashSet<Ontology>();
		}	
	

}
