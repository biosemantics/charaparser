package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class CSVKnowsSynonyms implements KnowsSynonyms {

	private IInflector inflector;
	private Map<String, Set<SynonymSet>> synonymSetsMap = new HashMap<String, Set<SynonymSet>>();
	
	public CSVKnowsSynonyms(String synonymsFile, IInflector inflector) {
		this.inflector = inflector;
		try(CSVReader reader = new CSVReader(new FileReader(synonymsFile))) {
			List<String[]> lines = reader.readAll();
			for(String[] line : lines) {
				String preferredTerm = line[0].trim().toLowerCase();
				Set<String> synonyms = new HashSet<String>();
				for(int i=0; i<line.length; i++) {
					String synonym = line[i].trim().toLowerCase();
					synonyms.add(synonym);
				}
				createSynonymSet(preferredTerm, synonyms);
			}	
		} catch(IOException e) {
			log(LogLevel.ERROR, "Can't read CSV", e);
		}
	}
	
	private void createSynonymSet(String preferredTerm, Set<String> synonyms) {
		SynonymSet synonymSet = new SynonymSet(preferredTerm, synonyms);
		
		Set<String> terms = new HashSet<String>(synonyms);
		terms.add(preferredTerm);
		for(String term : terms) {
			if(!synonymSetsMap.containsKey(term)) 
				synonymSetsMap.put(term, new HashSet<SynonymSet>());
			synonymSetsMap.get(term).add(synonymSet);
		}
	}

	@Override
	public Set<SynonymSet> getSynonyms(String term) {
		if(!synonymSetsMap.containsKey(term)) {
			Set<SynonymSet> defaultSet = new HashSet<SynonymSet>();
			Set<String> synonyms = new HashSet<String>();
			synonyms.add(term);
			SynonymSet synonymSet = new SynonymSet(term, synonyms);
			defaultSet.add(synonymSet);
			return defaultSet;
		}
		return synonymSetsMap.get(term);
	}

}