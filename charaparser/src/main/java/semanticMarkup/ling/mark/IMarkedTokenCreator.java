package semanticMarkup.ling.mark;

import semanticMarkup.ling.Token;

public interface IMarkedTokenCreator {

	public MarkedToken getMarkedToken(Token token);
	
}
