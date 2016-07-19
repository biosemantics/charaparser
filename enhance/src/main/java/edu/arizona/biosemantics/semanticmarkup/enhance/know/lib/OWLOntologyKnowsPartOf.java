package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.File;
import java.util.Set;

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
import edu.arizona.biosemantics.semanticmarkup.enhance.know.AnnotationProperty;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class OWLOntologyKnowsPartOf implements KnowsPartOf {

	private OWLAnnotationProperty labelProperty;
	private IInflector inflector;
	private String ontology;
	private OWLOntologyManager owlOntologyManager;
	private OWLOntology owlOntology;
	private OWLObjectProperty partOfProperty;

	public OWLOntologyKnowsPartOf(String ontology, IInflector inflector) throws OWLOntologyCreationException {
		this.ontology = ontology;
		this.owlOntologyManager = OWLManager.createOWLOntologyManager();
		this.owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(new File(ontology));
		this.inflector = inflector;
		this.partOfProperty = owlOntologyManager.getOWLDataFactory().getOWLObjectProperty(
				IRI.create(AnnotationProperty.PART_OF.getIRI()));
		this.labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(
				OWLRDFVocabulary.RDFS_LABEL.getIRI());
	}

	@Override
	public boolean isPartOf(String part, String parent) {
		Set<OWLClass> owlClasses = owlOntology.getClassesInSignature(Imports.INCLUDED);
		
		OWLClass partOwlClass = getOwlClassWithLabel(part, owlClasses);
		OWLClass parentOwlClass = getOwlClassWithLabel(parent, owlClasses);;
		if(partOwlClass == null) 
			partOwlClass = getOwlClassWithLabel(inflector.getSingular(part), owlClasses);
		if(parentOwlClass == null)
			parentOwlClass = getOwlClassWithLabel(inflector.getSingular(parent), owlClasses);
		
		if(partOwlClass != null && parentOwlClass != null) {
			OWLClassExpression partOfExpression = 
					owlOntologyManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(
							partOfProperty, parentOwlClass);
			OWLAxiom partOfAxiom = 
					owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(
							partOwlClass, partOfExpression);
			return owlOntology.containsAxiom(partOfAxiom, Imports.INCLUDED, AxiomAnnotations.CONSIDER_AXIOM_ANNOTATIONS);
		}
		return false;
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

	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyKnowsPartOf okp = new OWLOntologyKnowsPartOf("C:/OntologyOwlFilesTemp/999/on/on.owl", 
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
