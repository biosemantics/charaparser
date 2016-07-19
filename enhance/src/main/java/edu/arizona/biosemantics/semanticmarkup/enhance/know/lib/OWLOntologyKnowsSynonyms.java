package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
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
import edu.arizona.biosemantics.common.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
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
		OWLClass owlClass = getOwlClassWithLabelOrSynonym(term, owlClasses);
		
		String preferredTerm = term;
		Set<String> synonyms = new HashSet<String>();
		if(owlClass != null) {
			preferredTerm = getLabel(owlClass);
			synonyms = getSynonyms(owlClass);
		}
		
		SynonymSet synonymSet = new SynonymSet(preferredTerm, synonyms);	
		Set<SynonymSet> result = new HashSet<SynonymSet>();
		result.add(synonymSet);
		return result;
	}
	
	private Set<String> getSynonyms(OWLClass owlClass) {
		Set<String> synonyms = new HashSet<String>();
		for(OWLAnnotationAssertionAxiom axiom : EntitySearcher.getAnnotationAssertionAxioms(owlClass, owlOntology)) {
			if(axiom.getProperty().equals(this.exactSynonymProperty)) {
				OWLAnnotationValue annotationValue = axiom.getValue();
				if(annotationValue instanceof OWLLiteral) {
					String value = ((OWLLiteral) annotationValue).getLiteral();
					synonyms.add(value);
				}
			}
		}
		return synonyms;
	}

	private OWLClass getOwlClassWithLabelOrSynonym(String term, Set<OWLClass> owlClasses) {
		for(OWLClass owlClass : owlClasses) {
			String classLabel = getLabel(owlClass);
			if(classLabel != null) {
				if(classLabel.equals(term)) {
					return owlClass;
				}
			}
			
			for(OWLAnnotationAssertionAxiom axiom : EntitySearcher.getAnnotationAssertionAxioms(owlClass, owlOntology)) {
				if(axiom.getProperty().equals(this.exactSynonymProperty)) {
					OWLAnnotationValue annotationValue = axiom.getValue();
					if(annotationValue instanceof OWLLiteral) {
						String value = ((OWLLiteral) annotationValue).getLiteral();
						if(value.equals(term)) {
							return owlClass;
						}
					}
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
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyKnowsSynonyms oks = new OWLOntologyKnowsSynonyms("C:/OntologyOwlFilesTemp/999/on/on.owl", 
				new IInflector() {
					@Override
					public String getSingular(String word) {
						return word;
					}

					@Override
					public String getPlural(String word) {
						return word;
					}

					@Override
					public boolean isPlural(String word) {
						return false;
					}
		});
		System.out.println(oks.getSynonyms("hello").iterator().next().getPreferredTerm());
	}
	
}
