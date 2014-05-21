package edu.arizona.biosemantics.semanticmarkup.ling.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;


public interface IStemmer {
	
	public List<String> getStems(String word);

	public List<String> getStems(String word, POS pos);
	
}
