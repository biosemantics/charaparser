/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.ontologies;

import java.io.File;
import java.net.URI;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
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
	public OntologyFactory(@Named("Run_OntologyDirectory") String ontologyDirectory) {
		this.ontologyDirectory = ontologyDirectory;
	}
	
	public IOntology createOntology(Object locator){
		if(locator instanceof URI){
			return null; //for now
		}else if(locator instanceof String && ((String) locator).endsWith(".bin")){
			IOntology partOfBin = new PartOfFile(ontologyDirectory, (String)locator);
			if(partOfBin.objectCreated()) return partOfBin;
		}/*else if(locator instanceof String && ((String) locator).endsWith(".owl")){
			IOntology owlOntology = new OWLOntology(ontologyDirectory, (String)locator);
			if(owlOntology.objectCreated()) return owlOntology;
		}*/
		return null;
	}

}
