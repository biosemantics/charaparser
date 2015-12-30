package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.AnnotationProperty;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class OWLOntologyKnowsSynonyms implements KnowsSynonyms {

	private IInflector inflector;
	private String ontology;
	private OWLOntologyManager owlOntologyManager;
	private OWLOntology owlOntology;
	private OWLAnnotationProperty labelProperty;
	private OWLAnnotationProperty relatedSynonymProperty;
	private OWLAnnotationProperty narrowSynonymProperty;
	private OWLAnnotationProperty exactSynonymProperty;
	private OWLAnnotationProperty broadSynonymProperty;

	public OWLOntologyKnowsSynonyms(String ontology, IInflector inflector) throws OWLOntologyCreationException {
		this.ontology = ontology;
		this.owlOntologyManager = OWLManager.createOWLOntologyManager();
		this.owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(new File(ontology));
		this.inflector = inflector;
		this.labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(
				OWLRDFVocabulary.RDFS_LABEL.getIRI());
		this.relatedSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.RELATED_SYNONYM.getIRI()));
		this.narrowSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.NARROW_SYNONYM.getIRI()));
		this.exactSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
		this.broadSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.BROAD_SYNONYM.getIRI()));
	}
	
	@Override
	public Set<SynonymSet> getSynonyms(String term) {
		Set<OWLClass> owlClasses = owlOntology.getClassesInSignature(Imports.INCLUDED);
		OWLClass owlClass = getOwlClassWithLabel(term, owlClasses);
		if(owlClass == null) 
			owlClass = getOwlClassWithLabel(inflector.getSingular(term), owlClasses);
		
		Set<OWLClass> synonymOwlClasses = this.getSynonyms(owlClass, owlClasses);
		Set<String> synonyms = new HashSet<String>();
		String preferredTerm = term;
		synonyms.add(term);
		for(OWLClass synonymOwlClass : synonymOwlClasses) {
			String label = getLabel(synonymOwlClass);
			synonyms.add(label);
			if(label.length() < preferredTerm.length() || 
					(label.length() == preferredTerm.length() && label.compareTo(preferredTerm) < 0)) {
				preferredTerm = label;
			}
		}
		synonyms.remove(preferredTerm);
		
		// can we in ontology file differentiate synonym sets?
		SynonymSet synonymSet = new SynonymSet(preferredTerm, synonyms);	
		Set<SynonymSet> result = new HashSet<SynonymSet>();
		result.add(synonymSet);
		return result;
	}
	
	private Set<OWLClass> getSynonyms(OWLClass owlClass, Set<OWLClass> owlClasses) {
		String owlClassLabel = getLabel(owlClass);
		Set<OWLClass> synonymToTermClasses = new HashSet<OWLClass>();
		Set<OWLClass> termToSynonymClasses = new HashSet<OWLClass>();
		if(owlClass != null) {
			for(OWLClass possibleSynonym : owlClasses) {
				String possibleSynonymLabel = getLabel(possibleSynonym);
				OWLAnnotation synonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(
						exactSynonymProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(
								possibleSynonymLabel, "en"));
				OWLAxiom synonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(
						owlClass.getIRI(), synonymAnnotation);
				if(owlOntology.containsAxiom(synonymAxiom, 
						Imports.INCLUDED, AxiomAnnotations.CONSIDER_AXIOM_ANNOTATIONS)) {
					synonymToTermClasses.add(possibleSynonym);
				}
				
				synonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(
						exactSynonymProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(
								owlClassLabel, "en"));
				synonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(
						possibleSynonym.getIRI(), synonymAnnotation);
				if(owlOntology.containsAxiom(synonymAxiom, 
						Imports.INCLUDED, AxiomAnnotations.CONSIDER_AXIOM_ANNOTATIONS)) {
					termToSynonymClasses.add(possibleSynonym);
				}
			}	
		}
		
		// would have to search recursively for each synonym found for new synonyms of those?
		// how to determine preferred term?
		// is there a more efficient way to search than iterating all classes and seeing axiom is contained?
		Set<OWLClass> synonyms = new HashSet<OWLClass>();
		synonyms.addAll(synonymToTermClasses);
		synonyms.addAll(termToSynonymClasses);
		
		for(OWLClass synonym : new HashSet<OWLClass>(synonyms)) {
			synonyms.addAll(getSynonyms(synonym, owlClasses));
		}
		return synonyms;
	}
	
	private OWLClass getOwlClassWithLabel(String label, Set<OWLClass> owlClasses) {
		for(OWLClass owlClass : owlClasses) {
			String classLabel = getLabel(owlClass);
			if(classLabel != null) {
				if(classLabel.equals(label)) {
					return owlClass;
				}
			}
		}
		return null;
	}
	
	private String getLabel(OWLClass owlClass) {
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology, labelProperty)) {
			if (annotation.getValue() instanceof OWLLiteral) {
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				//if (val.hasLang("en")) {
				return val.getLiteral();
				//}
			}
		}
		return null;
	}

}
