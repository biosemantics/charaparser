package semanticMarkup.ling.transform;

import java.util.List;

import semanticMarkup.ling.Token;

public interface ITokenizer {

	public List<Token> tokenize(String text);
	
}
