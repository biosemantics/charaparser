package semanticMarkup.know;

public interface ICharacterKnowledgeBase {

	public String getCharacter(String word);
	
	public boolean contains(String word);

	public void addCharacter(String word, String character);
	
}
