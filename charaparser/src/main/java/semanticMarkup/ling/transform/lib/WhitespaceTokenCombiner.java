package semanticMarkup.ling.transform.lib;

import java.util.List;

import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenCombiner;

public class WhitespaceTokenCombiner implements ITokenCombiner {

	@Override
	public String combine(List<Token> tokens) {
		StringBuilder textBuilder = new StringBuilder();
		for(Token token : tokens) {
			textBuilder.append(token.getContent() + " ");
		}
		return textBuilder.toString().trim();
	}

}
