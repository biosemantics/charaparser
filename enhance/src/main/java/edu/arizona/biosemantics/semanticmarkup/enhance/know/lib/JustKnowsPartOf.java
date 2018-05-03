package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.ArrayList;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

/**
 * 
 * @author hongcui
 *
 * knows partof relations between two biological entities, regardless of the source of knoweldge (ontology or csv files)
 * 
 */
public class JustKnowsPartOf implements KnowsPartOf {
	private ArrayList<KnowsPartOf> knows = new ArrayList<KnowsPartOf> ();


	public JustKnowsPartOf(ArrayList<String> partOfFiles, ArrayList<String> ontologies, KnowsSynonyms knowsSynonyms, IInflector inflector){
		try{
			//add knowledge entities in the order of its precedence -- most reliable first
			OWLOntologyKnowsPartOf ooKnows = new OWLOntologyKnowsPartOf(ontologies, inflector);
			if(ooKnows!= null){
				knows.add(ooKnows);
			}
			log(LogLevel.DEBUG, "JustKnowsPartOf constructed OWLOntologyKnowsPartOf with at least one ontology successfully");
		}catch(Exception e){
			log(LogLevel.DEBUG, "JustKnowsPartOf failed to construct OWLOntologyKnowsPartOf:", e);
		}
		try{				
			CSVKnowsPartOf csvKnows = new CSVKnowsPartOf(partOfFiles, knowsSynonyms, inflector);
			if(csvKnows!= null){
				knows.add(csvKnows);
			}
			log(LogLevel.DEBUG, "JustKnowsPartOf constructed CSVKnowsPartOf successfully");
		}catch(Exception e){
			log(LogLevel.DEBUG, "JustKnowsPartOf failed to construct CSVKnowsPartOf:", e);
		}
	}
	
	@Override
	public boolean isPartOf(String part, String parent) {
		boolean isPartOf = false;
		for(KnowsPartOf know: knows){
			isPartOf = know.isPartOf(part, parent);
			if(isPartOf) return isPartOf;
		}
		return isPartOf;
	}

}
