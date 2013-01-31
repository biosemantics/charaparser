package semanticMarkup.ling.transform;

import semanticMarkup.ling.Token;
import edu.stanford.nlp.ling.HasWord;

public interface IStanfordParserTokenTransformer {

	public HasWord transform(Token token);
	
}
