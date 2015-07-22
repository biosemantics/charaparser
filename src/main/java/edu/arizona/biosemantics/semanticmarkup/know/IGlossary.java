package edu.arizona.biosemantics.semanticmarkup.know;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import edu.arizona.biosemantics.semanticmarkup.know.lib.Term;

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
	
	public Set<Term> getInfo(String word);
	/**
	 * @param category
	 * @return word in the category
	 */
	public Set<String> getWordsInCategory(String category);

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
	
	
	/**
	 * a synonym and its preferred term [label] always share the same category.
	 * @param syn
	 * @param category
	 * @param label
	 */
	public void addSynonym(String synonym, String category, String term);

	/**
	 * get all phrases with words connected by  "_".
	 * @return
	 */
	public HashSet<String> getPhrases();

	/**
	 * get all entity phrases with words connected by  "_" and replace "_" with " ";
	 * @return
	 */
	public HashSet<String> getStructurePhrasesWithSpaces(); 
	
	/**
	 * get all quality phrases with words connected by  "_" and replace "_" with " ";
	 * @return
	 */
	public HashSet<String> getNonStructurePhrasesWithSpaces();

	public Set<String> getPerferedWordsInCategory(String category); 
	
	public boolean 	hasIndexedStructure ();
	public ConcurrentSkipListSet<String> getIndexedStructures();
	
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
