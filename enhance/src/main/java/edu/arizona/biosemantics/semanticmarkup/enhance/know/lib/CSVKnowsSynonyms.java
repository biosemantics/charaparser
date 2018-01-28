package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class CSVKnowsSynonyms implements KnowsSynonyms {

	private IInflector inflector;
	private Map<String, Set<SynonymSet>> synonymSetsMap = new HashMap<String, Set<SynonymSet>>(); //key: term:category
	//synonymsFile is the output from term Review
	//after term review step is done, output the categories and synonyms files.
	public CSVKnowsSynonyms(String synonymsFile, IInflector inflector) {
		this.inflector = inflector;
		Map<String, Set<String>> content = new HashMap<String, Set<String>> (); //key: term:category
		try(CSVReader reader = new CSVReader(new FileReader(synonymsFile))) {
			//sort synonyms with their preferred terms
			List<String[]> lines = reader.readAll();
			for(String[] line : lines) {
				//line: preferred term, category, syn, termID (uuid)
				String preferredTerm = line[0].trim().toLowerCase(); //first token
				String category = line[1].trim().toLowerCase();
				String synonym = line[2].trim().toLowerCase();
				
				Set<String> synonyms = content.get(preferredTerm+":"+category);
				if(synonyms==null){
					synonyms = new HashSet<String>();
				}
				synonyms.add(synonym);
				content.put(preferredTerm+":"+category, synonyms);
				/*Set<String> synonyms = new HashSet<String>();
				for(int i=2; i<line.length; i++) { //subsequent tokens
					String synonym = line[i].trim().toLowerCase();
					synonyms.add(synonym);
				}
				createSynonymSet(preferredTerm, synonyms);*/
			}	
			//create synonym sets
			Set<String> pTerms = content.keySet();
			Iterator<String> preferredTermIt = pTerms.iterator();
			while(preferredTermIt.hasNext()){
				String pTerm = preferredTermIt.next();
				createSynonymSet(pTerm, content.get(pTerm));
			}
		} catch(IOException e) {
			log(LogLevel.ERROR, "Can't read CSV", e);
		}
	}
	
	/**
	 * pTerm = term:category
	 * @param pTerm
	 * @param synonyms
	 */
	private void createSynonymSet(String pTerm, Set<String> synonyms) {
		String [] termAndCategory = pTerm.split(":");
		String term = termAndCategory[0];
		String category = termAndCategory.length>1? termAndCategory[1]:"";
		
		SynonymSet synonymSet = new SynonymSet(term, category, synonyms);
		
		Set<String> terms = new HashSet<String>(synonyms);

		for(String aterm : terms) {
			String termEntry = aterm+":"+category;
			if(!synonymSetsMap.containsKey(termEntry)) 
				synonymSetsMap.put(termEntry, new HashSet<SynonymSet>());
			synonymSetsMap.get(termEntry).add(synonymSet);
		}
		
		if(!synonymSetsMap.containsKey(pTerm)) 
			synonymSetsMap.put(pTerm, new HashSet<SynonymSet>());
		synonymSetsMap.get(pTerm).add(synonymSet);
	}

	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		//wrapping up term and category in an object will make the search more costly
		String termEntry = term+":"+category;
		if(!synonymSetsMap.containsKey(termEntry)) {
			Set<SynonymSet> defaultSet = new HashSet<SynonymSet>();
			Set<String> synonyms = new HashSet<String>();
			synonyms.add(term);
			SynonymSet synonymSet = new SynonymSet(term, category, synonyms); //an empty synset with term as the prefered term
			defaultSet.add(synonymSet);
			return defaultSet;
		}
		return synonymSetsMap.get(termEntry);
		
		/*if(!synonymSetsMap.containsKey(termEntry)) {
			Set<SynonymSet> defaultSet = new HashSet<SynonymSet>();
			Set<String> synonyms = new HashSet<String>();
			synonyms.add(term);
			SynonymSet synonymSet = new SynonymSet(term, category, synonyms); //an empty synset with term as the prefered term
			defaultSet.add(synonymSet);
			return defaultSet;
		}
		return synonymSetsMap.get(termEntry);*/
	}

}