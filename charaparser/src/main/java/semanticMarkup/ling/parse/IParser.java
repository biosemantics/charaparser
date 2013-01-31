package semanticMarkup.ling.parse;

import java.util.List;

import semanticMarkup.ling.Token;


public interface IParser {

	public AbstractParseTree parse(List<? extends Token> sentence);
	
}
