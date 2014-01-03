package edu.arizona.sirls.semanticMarkup.know;

/**
 * An IOrganStateKnowledgeBase allows the management and lookup of organs and states
 * @author rodenhausen
 */
public interface IOrganStateKnowledgeBase {

	/**
	 * @param word
	 * @return if the given word is an organ
	 */
	public boolean isOrgan(String word);
	
	/**
	 * @param word
	 * @return if the given word is a state
	 */
	public boolean isState(String word);

	/**
	 * @param word to add as state
	 */
	public void addState(String word);
	
	/**
	 * @param word to add as organ
	 */
	public void addOrgan(String word);

	/**
	 * @param word
	 * @return if the word is contained in the knowledgebase
	 */
	public boolean contains(String word);
	
}
