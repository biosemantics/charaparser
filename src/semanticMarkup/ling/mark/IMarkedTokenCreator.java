package semanticMarkup.ling.mark;

import semanticMarkup.ling.Token;

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
