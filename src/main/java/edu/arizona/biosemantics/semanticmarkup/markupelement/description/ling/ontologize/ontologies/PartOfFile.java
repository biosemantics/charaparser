/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.ontologies;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology;

/**
 * @author Hong Cui
 *
 */

public class PartOfFile implements IOntology {

	private static Hashtable<String, ArrayList<String>> partof = null;
	private File file;
	
	@Inject
	public PartOfFile(@Named("Run_OutDirectory") String runOutDirectory){
		try{
		file = new File("workspace/fungi/", "ontopartof.bin");
		//read in serialized partof hashtable
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				file));
		// Deserialize the object
		if(partof ==null)
			partof = (Hashtable<String, ArrayList<String>>) in.readObject();  
		}catch(Exception e){
			e.printStackTrace();
			log(LogLevel.ERROR, e.toString());
		}

	}
	
	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology#isPart(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isPart(String part, String parent) {
		if(parent.compareTo("whole_organism")==0) parent = "organism";
		return partof.get(part)==null? false : partof.get(part).contains(parent);
	}

}
