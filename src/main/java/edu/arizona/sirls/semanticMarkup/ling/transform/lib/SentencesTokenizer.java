package edu.arizona.sirls.semanticMarkup.ling.transform.lib;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.sirls.semanticMarkup.ling.Token;
import edu.arizona.sirls.semanticMarkup.ling.transform.ITokenizer;


/**
 * SentenceTokenizer tokenizes a text by sentence termination punctuation and whitespace.
 * @author rodenhausen
 */
public class SentencesTokenizer implements ITokenizer {

	@Override
	public List<Token> tokenize(String text) {
		List<Token> tokens = new ArrayList<Token>();
		for(String token : text.split("(?;|.|!|?)+\\s+")) {
			tokens.add(new Token(token));
		}
		return tokens;
	}

}
