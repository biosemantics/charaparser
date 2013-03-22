package semanticMarkup.ling.transform.lib;

import semanticMarkup.ling.Token;
import semanticMarkup.ling.pos.POSedToken;
import semanticMarkup.ling.transform.IStanfordParserTokenTransformer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;

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
