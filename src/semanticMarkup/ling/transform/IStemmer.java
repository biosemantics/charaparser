package semanticMarkup.ling.transform;

import java.util.List;

import semanticMarkup.ling.pos.POS;

public interface IStemmer {
	
	public List<String> getStems(String word);

	public List<String> getStems(String word, POS pos);
	
}
