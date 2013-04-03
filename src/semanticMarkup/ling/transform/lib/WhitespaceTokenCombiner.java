package semanticMarkup.ling.transform.lib;

import java.util.List;

import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenCombiner;

/**
 * WhitespaceTokenCombiner combintes a list of tokens by whitespace
 * @author rodenhausen
 */
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
