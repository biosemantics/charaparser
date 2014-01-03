package edu.arizona.sirls.semanticMarkup.ling.transform;

import edu.arizona.sirls.semanticMarkup.ling.Token;
import edu.stanford.nlp.ling.HasWord;

/**
 * IStanfordParserTokenTransformer transforms semanticMarkup.ling.Token types to the stanford parser equivalent tokens: HasWord
 * TODO: interface probably not needed, shouldnt be a piece of much variablity
 * @author rodenhausen
 */
public interface IStanfordParserTokenTransformer {

	/**
	 * @param token
	 * @return the HashWord equivalent token
	 */
	public HasWord transform(Token token);
	
}
