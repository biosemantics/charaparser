package edu.arizona.biosemantics.semanticmarkup.ling.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;


/**
 * ITokenizer splits a String into a list of Tokens. Hence it operates in opposite direction of ITokenCombiner.
 * @author rodenhausen
 */
public interface ITokenizer {

	/**
	 * @param text
	 * @return List of Tokens representation of the text
	 */
	public List<Token> tokenize(String text);
	
}
