package edu.arizona.biosemantics.semanticmarkup.ling.transform.lib;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POSedToken;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IStanfordParserTokenTransformer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;

/**
 * WordStanfordParserTokenTransformer transforms semanticmarkup.ling.Token types to the stanford parser equivalent tokens: HasWord
 * @author rodenhausen
 */
public class WordStanfordParserTokenTransformer implements IStanfordParserTokenTransformer {

	@Override
	public HasWord transform(Token token) {
		if(token instanceof POSedToken) {
			POSedToken posedToken = (POSedToken)token;
			return new TaggedWord(posedToken.getContent(), posedToken.getPOS().toString());
		}
		return new Word(token.getContent());
	}

}
