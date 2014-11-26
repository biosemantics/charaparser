package edu.arizona.biosemantics.semanticmarkup.know;

import edu.arizona.biosemantics.semanticmarkup.know.lib.Match;

/**
 * An ICharacterKnowledgeBase allows the management and lookup of character names for character states
 * @author rodenhausen
 */
public interface ICharacterKnowledgeBase {

	/**
	 * @param characterState
	 * @return character name of the character state or null if no character name exists for the state
	 */
	//public String getCharacterName(String characterState);
	public Match getCharacterName(String characterState);
	
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
	//public void addCharacterStateToName(String characterState, String characterName);
	public void addCharacterStateToName(String characterState, Match match);

	/**
	 * @param characterName
	 * @return if the character name is contained in the knowledgebase 
	 */
	public boolean containsCharacterName(String characterName);

	public boolean isState(String terminalsText);
	
	public boolean isEntity(String terminalsText);

	/**
	 * must call isEntity(String) before calling getEntityType
	 * @param singular
	 * @param organName
	 * @return
	 */
	public String getEntityType(String singular, String organName);
	
}
