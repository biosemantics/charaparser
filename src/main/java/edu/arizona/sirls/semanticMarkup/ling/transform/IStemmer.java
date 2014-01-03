package edu.arizona.sirls.semanticMarkup.ling.transform;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.ling.pos.POS;


public interface IStemmer {
	
	public List<String> getStems(String word);

	public List<String> getStems(String word, POS pos);
	
}
