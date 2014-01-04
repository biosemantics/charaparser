package edu.arizona.biosemantics.semanticmarkup.know;

/**
 * An ICharacterKnowledgeBase allows the management and lookup of character names for character states
 * @author rodenhausen
 */
public interface ICharacterKnowledgeBase {

	/**
	 * @param characterState
	 * @return character name of the character state or null if no character name exists for the state
	 */
	public String getCharacterName(String characterState);
	
	/**
	 * @param characterState
	 * @return if the character state is contained in the knowledgebase
	 */
	public boolean containsCharacterState(String characterState);

	/**
	 * add a character state to the character name
	 * @param characterState
	 * @param characterName
	 */
	public void addCharacterStateToName(String characterState, String characterName);

	/**
	 * @param characterName
	 * @return if the character name is contained in the knowledgebase 
	 */
	public boolean containsCharacterName(String characterName);
	
}
