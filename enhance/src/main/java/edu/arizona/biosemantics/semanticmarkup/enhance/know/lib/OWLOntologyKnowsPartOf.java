package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.AnnotationProperty;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class OWLOntologyKnowsPartOf implements KnowsPartOf {

	private OWLAnnotationProperty labelProperty;
	private IInflector inflector;
	private ArrayList<String> ontologies;
	private OWLOntologyManager owlOntologyManager;
	private ArrayList<OWLOntology> owlOntologies;
	private OWLObjectProperty partOfProperty;

	public OWLOntologyKnowsPartOf(ArrayList<String> ontologies, IInflector inflector) throws Exception {
		boolean success = false;
		try{
			this.ontologies = ontologies;
			this.owlOntologyManager = OWLManager.createOWLOntologyManager();
			this.inflector = inflector;
			this.partOfProperty = owlOntologyManager.getOWLDataFactory().getOWLObjectProperty(
					IRI.create(AnnotationProperty.PART_OF.getIRI()));
			this.labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(
					OWLRDFVocabulary.RDFS_LABEL.getIRI());
			
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
		if(! success)
			throw (new OWLOntologyCreationException ("Can't read any of OWLOntology KnowsPartOf"));
	}

	/**
	 * return true if anyone of the ontologies returns true
	 */
	@Override
	public boolean isPartOf(String part, String parent) {
		for(OWLOntology owlOntology: owlOntologies){
			Set<OWLClass> owlClasses = owlOntology.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet());
			
			OWLClass partOwlClass = getOwlClassWithLabel(part,owlOntology, owlClasses);
			OWLClass parentOwlClass = getOwlClassWithLabel(parent, owlOntology, owlClasses);;
			if(partOwlClass == null) 
				partOwlClass = getOwlClassWithLabel(inflector.getSingular(part), owlOntology, owlClasses);
			if(parentOwlClass == null)
				parentOwlClass = getOwlClassWithLabel(inflector.getSingular(parent), owlOntology, owlClasses);
			
			if(partOwlClass != null && parentOwlClass != null) {
				OWLClassExpression partOfExpression = 
						owlOntologyManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(
								partOfProperty, parentOwlClass);
				OWLAxiom partOfAxiom = 
						owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(
								partOwlClass, partOfExpression);
				return owlOntology.containsAxiom(partOfAxiom, Imports.INCLUDED, AxiomAnnotations.CONSIDER_AXIOM_ANNOTATIONS);
			}
		}
		return false;
	}

	private OWLClass getOwlClassWithLabel(String label, OWLOntology ontology, Set<OWLClass> owlClasses) {
		for(OWLClass owlClass : owlClasses) {
			String classLabel = getLabel(owlClass, ontology);
			if(classLabel != null) {
				if(classLabel.equals(label)) {
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

	public static void main(String[] args) throws Exception {
		ArrayList<String> ontologies = new ArrayList<String> ();
		ontologies.add("C:/OntologyOwlFilesTemp/999/on/on.owl");
		OWLOntologyKnowsPartOf okp = new OWLOntologyKnowsPartOf(ontologies, 
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
		System.out.println(okp.isPartOf("futbol2", "futbol"));
		System.out.println(okp.isPartOf("futbol", "futbol2"));
	}
}
