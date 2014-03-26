package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;


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
	protected ConcurrentHashMap<String, Set<String>> glossary = new ConcurrentHashMap<String, Set<String>>();
	protected ConcurrentHashMap<String, Set<String>> reverseGlossary = new ConcurrentHashMap<String, Set<String>>();
	protected ConcurrentHashMap<String, Set<Term>> syns = new ConcurrentHashMap<String, Set<Term>>(); //syn => label, category
	protected ConcurrentHashMap<String, Set<Term>> reverseSyns = new ConcurrentHashMap<String, Set<Term>>(); //label => syn, catgory
	@Override
	public Set<String> getWords(String category) { //returns only preferred terms
		category = category.toLowerCase().trim();
		if(reverseGlossary.containsKey(category))
			return reverseGlossary.get(category);
		else
			return new HashSet<String>();
	}

	@Override
	public boolean contains(String word) {
		word = word.toLowerCase().trim();
		return glossary.containsKey(word) || syns.containsKey(word);
	}

	@Override
	public Set<String> getCategories(String word) {
		Set<Term> results = getInfo(word);
		Set<String> cats = new HashSet<String>();
		Iterator<Term> it = results.iterator();
		while(it.hasNext()){
			Term t = it.next();
			cats.add(t.getCategory());
		}
		return cats;
	}

	@Override
	public Set<Term> getInfo(String word) {
		word = word.toLowerCase().trim();
		Set<Term> results = new HashSet<Term>();
		if(glossary.containsKey(word)){
			Set<String> cats = glossary.get(word);
			for(String cat: cats){
				results.add(new Term(word, cat));
			}
			return results;
		}
		else if(syns.containsKey(word)){
			return syns.get(word);
		}else					
			return new HashSet<Term>();
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
	
	@Override
	public void addSynonym(String syn, String category, String label) {
		syn = syn.toLowerCase().trim();
		label = label.toLowerCase().trim();
		category = category.toLowerCase().trim();
		
		if(!syns.containsKey(syn))
			syns.put(syn, new HashSet<Term>());
		syns.get(syn).add( new Term (label, category));
		if(!reverseSyns.containsKey(label))
			reverseSyns.put(label, new HashSet<Term>());
		reverseSyns.get(label).add(new Term(syn, category));
	}


}
