package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public enum AnnotationProperty {

	PART_OF("http://purl.obolibrary.org/obo/BFO_0000050"), 
	LABEL(OWLRDFVocabulary.RDFS_LABEL.getIRI().toString()),
	SYNONYM("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"),
	DEFINITION("http://purl.obolibrary.org/obo/IAO_0000115"), 
	CREATION_DATE("http://www.geneontology.org/formats/oboInOwl#creation_date"),
	CREATED_BY("http://www.geneontology.org/formats/oboInOwl#created_by"),
	RELATED_SYNONYM("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"),
	NARROW_SYNONYM("http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym"),
	EXACT_SYNONYM("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"),
	BROAD_SYNONYM("http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym");
	
	private String iri;
	
	AnnotationProperty(String iri) {
		this.iri = iri;
	}
	
	public String getIRI() {
		return iri;
	}
}
