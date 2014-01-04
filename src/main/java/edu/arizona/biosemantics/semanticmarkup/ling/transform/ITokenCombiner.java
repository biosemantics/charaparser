package edu.arizona.biosemantics.semanticmarkup.ling.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;


/**
 * ITokenCombiner combines a list of Tokens to a String. Hence it operates in opposite direction of ITokenizer.
 * @author rodenhausen
 */
public interface ITokenCombiner {

	/**
	 * @param tokens
	 * @return String representation of all tokens
	 */
	public String combine(List<Token> tokens);
	
}
