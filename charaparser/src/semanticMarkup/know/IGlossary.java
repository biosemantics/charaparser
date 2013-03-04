package semanticMarkup.know;

import java.util.Set;

public interface IGlossary {

	public Set<String> getCategories(String word);
	
	public Set<String> getWords(String category);

	public boolean contains(String word);
	
	public Set<String> getWordsNotInCategories(Set<String> categories);

	public Set<String> getCategories();
	
}
