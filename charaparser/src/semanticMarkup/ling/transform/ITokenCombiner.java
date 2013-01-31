package semanticMarkup.ling.transform;

import java.util.List;

import semanticMarkup.ling.Token;

public interface ITokenCombiner {

	public String combine(List<Token> tokens);
	
}
