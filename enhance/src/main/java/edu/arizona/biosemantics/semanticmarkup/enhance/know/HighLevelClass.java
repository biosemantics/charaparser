package edu.arizona.biosemantics.semanticmarkup.enhance.know;

public enum HighLevelClass {

	ENTITY("http://purl.obolibrary.org/obo/CARO_0000006"), //material anatomical entity
	QUALITY("http://purl.obolibrary.org/obo/PATO_0000001"); //quality
	
	private String iri;
	
	HighLevelClass(String iri) {
		this.iri = iri;
	}
	
	public String getIRI() {
		return iri;
	}
}
