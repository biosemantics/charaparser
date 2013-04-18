package semanticMarkup.know;

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
	
}
