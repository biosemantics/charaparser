/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.enhance.know.partof;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.arizona.biosemantics.common.log.LogLevel;

/**
 * @author updates
 *
 */
public class PartOfOntology implements IOntology {

	/**
	 * 
	 */
	boolean success = false;
	OWLOntology ontology = null;
	
	public PartOfOntology(String directory, String file) {
		 OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			try {
				ontology = manager.loadOntologyFromOntologyDocument(new File(directory, file));
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
				log(LogLevel.DEBUG, "Failed to load file "+file +" from directory "+directory);
				return;
			}
	        success = true;
	}

	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology#isPart(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isPart(String part, String parent) {
		//TODO
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology#objectCreated()
	 */
	@Override
	public boolean objectCreated() {
		return success;
	}

}
