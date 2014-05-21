package edu.arizona.biosemantics.semanticmarkup.ling.transform;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;
import edu.stanford.nlp.ling.HasWord;

/**
 * IStanfordParserTokenTransformer transforms semanticmarkup.ling.Token types to the stanford parser equivalent tokens: HasWord
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
