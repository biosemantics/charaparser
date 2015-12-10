package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class Ontology implements KnowsSynonyms {

	private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
	private OWLOntology owlOntology;
	private File file;
	
	private OWLClass entityClass;
	private OWLClass qualityClass;
	private OWLObjectProperty partOfProperty;
	private OWLAnnotationProperty labelProperty;
	private OWLAnnotationProperty synonymProperty;
	private OWLAnnotationProperty definitionProperty;
	private OWLAnnotationProperty creationDateProperty;
	private OWLAnnotationProperty createdByProperty;
	private OWLAnnotationProperty relatedSynonymProperty;
	private OWLAnnotationProperty narrowSynonymProperty;
	private OWLAnnotationProperty exactSynonymProperty;
	private OWLAnnotationProperty broadSynonymProperty;

	public Ontology(File file) throws OWLOntologyCreationException {
		this.file = file;
		owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(file);
		
		entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(HighLevelClass.ENTITY.getIRI())); //material anatomical entity
		qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(HighLevelClass.QUALITY.getIRI())); //quality
		partOfProperty = owlOntologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(AnnotationProperty.PART_OF.getIRI()));
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.LABEL.getIRI()));
		synonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.SYNONYM.getIRI()));
		definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.DEFINITION.getIRI()));
		creationDateProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATION_DATE.getIRI()));
		createdByProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATED_BY.getIRI()));
		relatedSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.RELATED_SYNONYM.getIRI()));
		narrowSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.NARROW_SYNONYM.getIRI()));
		exactSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
		broadSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.BROAD_SYNONYM.getIRI()));
	}
	
	@Override
	public Set<SynonymSet> getSynonyms(String term) {
		Set<SynonymSet> result = new HashSet<SynonymSet>();
		OWLClass owlClass = getOwlClassForLabel(term);
		if(owlClass != null) {
			/*
			OWLAnnotation synonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(exactSynonymProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(synonym.getSynonym(), "en"));
			OWLAxiom synonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), synonymAnnotation);
			owlOntologyManager.addAxiom(owlOntology, synonymAxiom);
			*/
			//which synonym property, see above?.
			
			//  how to determine preferred term? look at language = en, term with lowest id?
			for(OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology)) {
				if(annotation.getProperty().equals(exactSynonymProperty)) {
					OWLAnnotationValue annotationValue = annotation.getValue();
					annotationValue.asLiteral().get().getLiteral();
					
				}
			}
		}
		return result;
	}
	
	public OWLClass getOwlClassForLabel(String label) {
		for(OWLClass owlClass: owlOntology.getClassesInSignature()){
			
			// Get the annotations on the class that use the label property
			for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology, owlOntologyManager.getOWLDataFactory().getRDFSLabel())) {
			    if (annotation.getValue() instanceof OWLLiteral) {
			        OWLLiteral val = (OWLLiteral) annotation.getValue();
			        if (val.getLiteral().equals(label)) {
			        	return owlClass;
			        }
			    }
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		Ontology ontology = new Ontology(new File("C:\\OntologyOwlFiles\\real\\po.owl"));
		ontology.getSynonyms("non-vascular leaf initial cell");
	}

}
