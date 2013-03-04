package semanticMarkup.know;

public interface ICharacterKnowledgeBase {

	public String getCharacterName(String characterState);
	
	public boolean containsCharacterState(String characterState);

	public void addCharacterStateToName(String characterState, String characterName);

	public boolean containsCharacterName(String terminalsText);
	
}
