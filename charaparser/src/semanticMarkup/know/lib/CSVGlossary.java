package semanticMarkup.know.lib;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * CSVGlossary expects a CSV file in the format word;category
 * @author thomas rodenhausen
 */
public class CSVGlossary implements IGlossary {

	private HashMap<String, Set<String>> glossary = new HashMap<String, Set<String>>();
	private HashMap<String, Set<String>> reverseGlossary = new HashMap<String, Set<String>>();
	
	@Inject
	public CSVGlossary(@Named("CSVGlossary_filePath") String filePath) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(filePath));
		
		List<String[]> lines = reader.readAll();
		for(String[] line : lines) {
			
			String term = line[1].toLowerCase();
			String category = line[2].toLowerCase();
			
			if(!glossary.containsKey(term))
				glossary.put(term, new HashSet<String>());
			glossary.get(term).add(category);
			
			if(!reverseGlossary.containsKey(category))
				reverseGlossary.put(category, new HashSet<String>());
			reverseGlossary.get(category).add(term);
		}
		
		reader.close();
	}
	
	public Set<String> getWords(String category) {
		category = category.toLowerCase();
		if(reverseGlossary.containsKey(category))
			return reverseGlossary.get(category);
		else
			return new HashSet<String>();
	}

	public boolean contains(String word) {
		word = word.toLowerCase();
		return glossary.containsKey(word);
	}

	public Set<String> getCategories(String word) {
		word = word.toLowerCase();
		if(glossary.containsKey(word))
			return glossary.get(word);
		else
			return new HashSet<String>();
	}

	@Override
	public Set<String> getWordsNotInCategories(Set<String> categories) {
		Set<String> result = new HashSet<String>();
		for(String category : this.glossary.keySet()) {
			category = category.toLowerCase();
			if(!categories.contains(category))
				result.addAll(glossary.get(category));
		}
		return result;
	}

	@Override
	public Set<String> getCategories() {
		return reverseGlossary.keySet();
	}
}
