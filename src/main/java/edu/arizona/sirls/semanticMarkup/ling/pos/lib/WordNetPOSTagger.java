package edu.arizona.sirls.semanticMarkup.ling.pos.lib;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.sirls.semanticMarkup.know.IPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.Token;
import edu.arizona.sirls.semanticMarkup.ling.pos.IPOSTagger;
import edu.arizona.sirls.semanticMarkup.ling.pos.POS;
import edu.arizona.sirls.semanticMarkup.ling.pos.POSedToken;


/**
 * OrganCharacterPOSTagger taggs tokens using an IPOSKnowledgeBase
 * @author rodenhausen
 */
public class WordNetPOSTagger implements IPOSTagger {

	private IPOSKnowledgeBase posKnowledgeBase;

	/**
	 * @param posKnowledgeBase
	 */
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
