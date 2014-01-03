package edu.arizona.sirls.semanticMarkup.know;

import java.util.Set;

/**
 * An IGlossary allows to lookup categories of words and vice versa
 * @author rodenhausen
 *
 */
public interface IGlossary {

	/**
	 * @param word
	 * @return categories of the word
	 */
	public Set<String> getCategories(String word);
	
	/**
	 * @param category
	 * @return word in the category
	 */
	public Set<String> getWords(String category);

	/**
	 * @param word
	 * @return if the word is contained in the glossary
	 */
	public boolean contains(String word);
	
	/**
	 * @param categories
	 * @return words of the glossary that are not contained in the categories
	 */
	public Set<String> getWordsNotInCategories(Set<String> categories);
	
	/**
	 * @param word
	 * @param category
	 */
	public void addEntry(String word, String category);
	
	
	
	/*
	public void addStructure(String structure);
	
	public void addCharacterCategory(String category);
	
	*//**
	 * Adds the category if it didn't exist yet as well
	 * @param state
	 * @param category
	 *//*
	public void addCharacterState(String state, String category);
	
	public boolean isStructure(String structure);
	
	public boolean isCharacterCategory(String category);
	
	public boolean isCharacterState(String state);
	
	public boolean isCharacterStateCategoryPair(String state, String category);
	
	public Set<String> getCharacterStateCategories(String state);
	
	public Set<String> getCharacterCategoryStates(String category);
	
	public Set<String> getStructures();
	
	public Set<String> getCharacterCategories();
	
	public Set<String> getCharacterStatesNotInCharacterCategories(Set<String> categories);
	*/
}
