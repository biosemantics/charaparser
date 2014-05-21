package edu.arizona.biosemantics.semanticmarkup.know;

/**
 * An ICorpus allows lookup of word frequencies within the corpus
 * @author rodenhausen
 */
public interface ICorpus {
	
	/**
	 * @param word
	 * @return the frequency of the word in the corpus
	 */
	public int getFrequency(String word);

}
