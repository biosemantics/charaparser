package semanticMarkup.know.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.know.IGlossary;

public class InMemoryGlossary implements IGlossary {

/*	private Set<String> structures = new HashSet<String>();
	private Set<String> characterCategories = new HashSet<String>();
	private Map<String, Set<String>> characterCategoryStatesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> characterStateCategoriesMap = new HashMap<String, Set<String>>();
	private Set<String> characterStates = new HashSet<String>();
	
	@Override
	public void addStructure(String structure) {
		structure = structure.toLowerCase().trim();
		this.structures.add(structure);
	}

	@Override
	public void addCharacterCategory(String category) {
		category = category.toLowerCase().trim();
		this.characterCategories.add(category);
	}

	@Override
	public void addCharacterState(String state, String category) {
		state = state.toLowerCase().trim();
		category = category.toLowerCase().trim();
		if(!this.characterCategoryStatesMap.containsKey(category))
			this.characterCategoryStatesMap.put(category, new HashSet<String>());
		if(!this.characterStateCategoriesMap.containsKey(state))
			this.characterStateCategoriesMap.put(state, new HashSet<String>());
		this.characterCategoryStatesMap.get(category).add(state);
		this.characterStateCategoriesMap.get(state).add(category);
		this.characterCategories.add(category);
		this.characterStates.add(state);
	}

	@Override
	public boolean isStructure(String structure) {
		structure = structure.toLowerCase().trim();
		return this.structures.contains(structure.toLowerCase().trim());
	}

	@Override
	public boolean isCharacterCategory(String category) {
		category = category.toLowerCase().trim();
		return this.characterCategories.contains(category);
	}

	@Override
	public boolean isCharacterState(String state) {
		state = state.toLowerCase().trim();
		return this.characterStates.contains(state);
	}

	@Override
	public boolean isCharacterStateCategoryPair(String state, String category) {
		state = state.toLowerCase().trim();
		category = category.toLowerCase().trim();
		if(this.characterCategoryStatesMap.containsKey(category))
			return characterCategoryStatesMap.get(category).contains(state);
		return false;
	}

	@Override
	public Set<String> getCharacterStateCategories(String state) {
		state = state.toLowerCase().trim();
		return this.characterStateCategoriesMap.get(state);
	}

	@Override
	public Set<String> getCharacterCategoryStates(String category) {
		category = category.toLowerCase().trim();
		return this.characterCategoryStatesMap.get(category);
	}

	@Override
	public Set<String> getStructures() {
		return this.structures;
	}

	@Override
	public Set<String> getCharacterCategories() {
		return this.characterCategories;
	}

	@Override
	public Set<String> getCharacterStatesNotInCharacterCategories(
			Set<String> categories) {
		Set<String> normalizedCategories = new HashSet<String>();
		for(String category : categories)
			normalizedCategories.add(category.toLowerCase().trim());
		
		Set<String> result = new HashSet<String>();
		for(String category : this.characterCategoryStatesMap.keySet()) {
			if(!normalizedCategories.contains(category))
				result.addAll(this.characterCategoryStatesMap.get(category));
		}
		return result;
	}
*/
	protected HashMap<String, Set<String>> glossary = new HashMap<String, Set<String>>();
	protected HashMap<String, Set<String>> reverseGlossary = new HashMap<String, Set<String>>();
	
	@Override
	public Set<String> getWords(String category) {
		category = category.toLowerCase().trim();
		if(reverseGlossary.containsKey(category))
			return reverseGlossary.get(category);
		else
			return new HashSet<String>();
	}

	@Override
	public boolean contains(String word) {
		word = word.toLowerCase().trim();
		return glossary.containsKey(word);
	}

	@Override
	public Set<String> getCategories(String word) {
		word = word.toLowerCase().trim();
		if(glossary.containsKey(word))
			return glossary.get(word);
		else
			return new HashSet<String>();
	}

	@Override
	public Set<String> getWordsNotInCategories(Set<String> categories) {
		Set<String> normalizedCategories = new HashSet<String>();
		for(String category : categories)
			normalizedCategories.add(category.toLowerCase().trim());
	
		Set<String> result = new HashSet<String>();
		for(String category : this.reverseGlossary.keySet()) {
			if(!normalizedCategories.contains(category))
				result.addAll(reverseGlossary.get(category));
		}
		return result;
	}

	@Override
	public void addEntry(String word, String category) {
		word = word.toLowerCase().trim();
		category = category.toLowerCase().trim();
		if(!glossary.containsKey(word))
			glossary.put(word, new HashSet<String>());
		glossary.get(word).add(category);
		if(!reverseGlossary.containsKey(category))
			reverseGlossary.put(category, new HashSet<String>());
		reverseGlossary.get(category).add(word);
	}
}
