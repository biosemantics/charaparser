package semanticMarkup.know.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.know.IGlossary;

public class InMemoryGlossary implements IGlossary {

	protected HashMap<String, Set<String>> glossary = new HashMap<String, Set<String>>();
	protected HashMap<String, Set<String>> reverseGlossary = new HashMap<String, Set<String>>();
	
	@Override
	public Set<String> getWords(String category) {
		category = category.toLowerCase();
		if(reverseGlossary.containsKey(category))
			return reverseGlossary.get(category);
		else
			return new HashSet<String>();
	}

	@Override
	public boolean contains(String word) {
		word = word.toLowerCase();
		return glossary.containsKey(word);
	}

	@Override
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
		for(String category : this.reverseGlossary.keySet()) {
			category = category.toLowerCase();
			if(!categories.contains(category))
				result.addAll(reverseGlossary.get(category));
		}
		return result;
	}

	@Override
	public void addEntry(String word, String category) {
		glossary.get(word).add(category);
		reverseGlossary.get(category).add(word);
	}
}
