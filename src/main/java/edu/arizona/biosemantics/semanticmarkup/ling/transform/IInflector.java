package edu.arizona.biosemantics.semanticmarkup.ling.transform;

/**
 * IInflector transforms words, e.g. from singular to plural
 * @author rodenhausen
 */
public interface IInflector {

	/**
	 * @param word
	 * @return the singular form of the word
	 */
	public String getSingular(String word);
	
	/**
	 * @param word
	 * @return the plural form of the word
	 */
	public String getPlural(String word);
	
	/**
	 * @param word
	 * @return if the word is plural
	 */
	public boolean isPlural(String word);
	
	/**
	 * @param word
	 * @return if the word is singular
	 */
	public boolean isSingular(String word);
	
}
