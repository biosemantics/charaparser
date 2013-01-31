package semanticMarkup.ling.pos;

import java.util.List;

import semanticMarkup.ling.Token;

public interface IPOSTagger {

	public List<Token> tag(List<Token> sentence);
	
}
