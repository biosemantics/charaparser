package edu.arizona.sirls.semanticMarkup.ling.mark;

import edu.arizona.sirls.semanticMarkup.ling.Token;

/**
 * IMarkedTokenCreator create marked tokens
 * @author rodenhausen
 */
public interface IMarkedTokenCreator {

	/**
	 * @param token
	 * @return a MarkedToken created from the token
	 */
	public MarkedToken getMarkedToken(Token token);
	
}
