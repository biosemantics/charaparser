package edu.arizona.sirls.semanticMarkup.know;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.ling.pos.POS;


/**
 * An IPOSKnowledgeBase allows the management, lookup, and verification of part of speech of words 
 * @author rodenhausen
 */
public interface IPOSKnowledgeBase {

	/**
	 * @param word
	 * @return if the word is a noun
	 */
	public boolean isNoun(String word);
	
	/**
	 * 
	 * @param word
	 * @return if the word is an adjective
	 */
	public boolean isAdjective(String word);
		
	/**
	 * 
	 * @param word
	 * @return if the word is an adverb
	 */
	public boolean isAdverb(String word);
	
	/**
	 * 
	 * @param word
	 * @return if the word is a verb
	 */
	public boolean isVerb(String word);
	
	/**
	 * 
	 * @param word
	 * @return the most likely pos tag of the word
	 */
	public POS getMostLikleyPOS(String word);
	
	/**
	 * 
	 * @param word
	 * @return if the word is contained in the knowledgebase
	 */
	public boolean contains(String word);

	/**
	 * 
	 * @param word
	 * @return the singular forms of the word
	 */
	public List<String> getSingulars(String word);
	
	/**
	 * @param word to add as verb
	 */
	public void addVerb(String word);
	
	/**
	 * @param word to add as noun
	 */
	public void addNoun(String word);
	
	/**
	 * @param word to add as adjective
	 */
	public void addAdjective(String word);
	
	/**
	 * @param word to add as adverb
	 */
	public void addAdverb(String word);
	
}
