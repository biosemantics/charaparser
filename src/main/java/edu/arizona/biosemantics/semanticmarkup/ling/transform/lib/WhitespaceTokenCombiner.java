package edu.arizona.biosemantics.semanticmarkup.ling.transform.lib;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenCombiner;


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
