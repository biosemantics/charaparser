/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.ontologies;

import java.io.File;
import java.net.URI;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology;

/**
 * @author Hong Cui
 *
 */
public class OntologyFactory {
	String ontologyDirectory;

	/**
	 * 
	 */
	@Inject
	public OntologyFactory(String ontologyDirectory) {
		this.ontologyDirectory = ontologyDirectory;
	}
	
	public IOntology createOntology(Object ontoLocator){
		if(ontoLocator instanceof URI){
			return null; //for now
		}else if(ontoLocator instanceof String && ((String) ontoLocator).endsWith(".bin")){
			IOntology partOfBin = new PartOfFile(ontologyDirectory, (String)ontoLocator);
			if(partOfBin.objectCreated()) return partOfBin;
		}else if(ontoLocator instanceof String && ((String) ontoLocator).endsWith(".owl")){
			IOntology owlOntology = new PartOfOntology(ontologyDirectory, (String)ontoLocator);
			if(owlOntology.objectCreated()) return owlOntology;
		}
		return null;
	}

}
