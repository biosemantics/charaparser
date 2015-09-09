/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.enhance.know.partof;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;

/**
 * @author Hong Cui
 * a serialized Hashtable<String, ArrayList<String>> holding part_of relations (part => parent)

 */

public class PartOfFile implements IOntology {

	private static Hashtable<String, ArrayList<String>> partof = null;
	private File file;
	private boolean success = false;

	@Inject
	public PartOfFile(String directory, String partOfBin){
		try{
			//file = new File(directory, "ontopartof.bin");
			file = new File(directory, partOfBin);
			if(!file.exists()){
				log(LogLevel.DEBUG, "File containing part_of relations not found. Disabled related functions");
				return;
			}
			//read in serialized partof hashtable
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			// Deserialize the object
			if(partof ==null){
				partof = (Hashtable<String, ArrayList<String>>) in.readObject();  
				success = true;
			}
		}catch(Exception e){
			log(LogLevel.ERROR, "Couldn't read `part of` file", e);
		}
	}

	public boolean objectCreated(){
		return success;
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
