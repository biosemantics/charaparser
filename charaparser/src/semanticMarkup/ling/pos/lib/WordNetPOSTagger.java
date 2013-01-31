package semanticMarkup.ling.pos.lib;

import java.util.ArrayList;
import java.util.List;

import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.pos.POSedToken;

public class WordNetPOSTagger implements IPOSTagger {

	private IPOSKnowledgeBase posKnowledgeBase;

	public WordNetPOSTagger(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}


	@Override
	public List<Token> tag(List<Token> sentence) {
		List<Token> taggedSentence = new ArrayList<Token>();
		for(Token token : sentence) {
			POS pos = posKnowledgeBase.getMostLikleyPOS(token.getContent());
			if(pos == null)
				pos = POS.NONE;
			taggedSentence.add(new POSedToken(token.getContent(), pos));
		}
		return taggedSentence;
	}

}
