/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.know;

/**
 * @author Hong Cui
 * Interface for a match of a word in a glossary. 
 * Searching a word in a glossary results in a match which may be empty or 
 * contain matched categories and preferred terms (if the search term is a synonym) 
 */
public interface IMatch {
	
	/**
	 * 
	 * @param category
	 * @return the preferred term of the category
	 */
	public String getLabel(String category);
	
	/**
	 * 
	 * @return null when the match containing more than one preferred term; the preferred term when the match containing one preferred term.
	 */
	public String getLabel();
	
	/**
	 * 
	 * @return all the categories in the match, connected by "_or_"
	 */
	public String getCategories();

}
