package semanticMarkup.know;

import java.util.List;

import semanticMarkup.ling.pos.POS;

public interface IPOSKnowledgeBase {

	public boolean isNoun(String word);
	
	public boolean isAdjective(String word);
		
	public boolean isAdverb(String word);
	
	public boolean isVerb(String word);
	
	public POS getMostLikleyPOS(String word);
	
	public boolean contains(String word);

	public List<String> getSingulars(String word);
	
	public void addVerb(String word);
	
	public void addNoun(String word);
	
	public void addAdjective(String word);
	
	public void addAdverb(String word);
	
}
