package edu.arizona.biosemantics.semanticmarkup.ling.pos;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;


/**
 * IPOSTagger transforms a list of tokens into a list of tokens (e.g. by adding the part of speech tags to the tokens) 
 * @author rodenhausen
 */
public interface IPOSTagger {

	/**
	 * @param sentence
	 * @return list of tokens
	 */
	public List<Token> tag(List<Token> sentence);
	
}
