package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
	private ArrayList<String> ontologies;
	private OWLOntologyManager owlOntologyManager;
	private ArrayList<OWLOntology> owlOntologies;
	private OWLAnnotationProperty labelProperty;
	private OWLAnnotationProperty relatedSynonymProperty;
	private OWLAnnotationProperty narrowSynonymProperty;
	private OWLAnnotationProperty exactSynonymProperty;
	private OWLAnnotationProperty broadSynonymProperty;

	public OWLOntologyKnowsSynonyms(ArrayList<String> ontologies, IInflector inflector) throws OWLOntologyCreationException {
		boolean success =false;
		try{
			this.ontologies = ontologies;
			this.owlOntologyManager = OWLManager.createOWLOntologyManager();
			this.inflector = inflector;
			this.labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(
					OWLRDFVocabulary.RDFS_LABEL.getIRI());
			this.relatedSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.RELATED_SYNONYM.getIRI()));
			this.narrowSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.NARROW_SYNONYM.getIRI()));
			this.exactSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
			this.broadSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.BROAD_SYNONYM.getIRI()));
			
			for(String ontology: ontologies){
				try{
					this.owlOntologies.add(owlOntologyManager.loadOntologyFromOntologyDocument(new File(ontology)));
					success = true;
				}catch(Exception e){
					log(LogLevel.DEBUG, "Can't read ontology file "+ontology, e);	
				}
			}
		}catch(Exception e){
			if(! success)
				throw (new OWLOntologyCreationException ("Can't read any of OWLOntology KnowsPartOf"));
			}
		}
	
	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		Set<SynonymSet> result = new HashSet<SynonymSet>();
		for(OWLOntology owlOntology: owlOntologies){
			Set<OWLClass> owlClasses = owlOntology.getClassesInSignature(Imports.INCLUDED);
			OWLClass owlClass = getOwlClassWithLabelOrSynonym(term, owlOntology, owlClasses);
			
			String preferredTerm = term;
			Set<String> synonyms = new HashSet<String>();
			if(owlClass != null) {
				preferredTerm = getLabel(owlClass, owlOntology);
				synonyms = getSynonyms(owlClass, owlOntology);
			}
			
			SynonymSet synonymSet = new SynonymSet(preferredTerm, category, synonyms);	
	
			result.add(synonymSet);
		}
		return result;
	}
	
	private Set<String> getSynonyms(OWLClass owlClass, OWLOntology owlOntology) {
		Set<String> synonyms = new HashSet<String>();
		for(OWLAnnotationAssertionAxiom axiom : EntitySearcher.getAnnotationAssertionAxioms(owlClass, owlOntology).collect(Collectors.toSet())) {
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

	private OWLClass getOwlClassWithLabelOrSynonym(String term, OWLOntology owlOntology, Set<OWLClass> owlClasses) {
		for(OWLClass owlClass : owlClasses) {
			for(OWLAnnotationAssertionAxiom axiom : EntitySearcher.getAnnotationAssertionAxioms(owlClass, owlOntology).collect(Collectors.toSet())) {
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
		for(OWLClass owlClass : owlClasses) {
			String classLabel = getLabel(owlClass, owlOntology);
			if(classLabel != null) {
				if(classLabel.equals(term)) {
					return owlClass;
				}
			}
		}
		return null;
	}
	
	private String getLabel(OWLClass owlClass, OWLOntology owlOntology) {
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology, labelProperty).collect(Collectors.toSet())) {
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
		ArrayList<String> ontologies = new ArrayList<String> ();
		ontologies.add("C:/OntologyOwlFilesTemp/999/on/on.owl");
		OWLOntologyKnowsSynonyms oks = new OWLOntologyKnowsSynonyms(ontologies, 
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
		System.out.println(oks.getSynonyms("hello", "").iterator().next().getPreferredTerm());
	}
	
}
