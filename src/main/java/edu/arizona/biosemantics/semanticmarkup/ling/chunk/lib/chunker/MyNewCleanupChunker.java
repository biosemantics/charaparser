package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;




import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * MyCleanupChunker reformats the chunks after all other chunkers have been run to obtain an overall consistent chunking result
 * Specifically modifier, character, constraint and organ chunks are reconsidered
 * @author rodenhausen
 */
public class MyNewCleanupChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param organStateKnowledgeBase
	 */
	@Inject
	public MyNewCleanupChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
		 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector,learnedCharacterKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		boolean[] translateCharacterToConstraint = new boolean[terminals.size()];
		boolean[] translateStateToConstraint = new boolean[terminals.size()];
		boolean[] translateModifierToConstraint = new boolean[terminals.size()];
		boolean[] translateStateToModifier = new boolean[terminals.size()];
		boolean[] translateConstraintToModifier = new boolean[terminals.size()];
		
		determineTranslationsBasedOnStructure(terminals, chunkCollector, translateCharacterToConstraint, translateStateToConstraint, translateModifierToConstraint,
				translateStateToModifier, translateConstraintToModifier);

		for(int i=0; i<terminals.size(); i++) {
			if(translateCharacterToConstraint[i])
				if(characterPermitsTranslation(i, terminals, chunkCollector)) {
					translate(ChunkType.CHARACTER_STATE, ChunkType.CONSTRAINT, i, chunkCollector);
					chunkCollector.addChunk(chunkCollector.getChunk(terminals.get(i)));
				}
			if(translateStateToConstraint[i]) {
				translate(ChunkType.STATE, ChunkType.CONSTRAINT, i, chunkCollector);
				chunkCollector.addChunk(chunkCollector.getChunk(terminals.get(i)));
			}
			if(translateModifierToConstraint[i]) {
				translate(ChunkType.MODIFIER, ChunkType.CONSTRAINT, i, chunkCollector);
				chunkCollector.addChunk(chunkCollector.getChunk(terminals.get(i)));
			}
			if(translateStateToModifier[i]) {
				translate(ChunkType.STATE, ChunkType.MODIFIER, i, chunkCollector);
				chunkCollector.addChunk(chunkCollector.getChunk(terminals.get(i)));
			}
			if(translateConstraintToModifier[i]) {
				translate(ChunkType.CONSTRAINT, ChunkType.MODIFIER, i, chunkCollector);
				chunkCollector.addChunk(chunkCollector.getChunk(terminals.get(i)));
			}
		}
		
		//capture multiple modifiers following each other into one chunk
		Chunk previousModifierChunk = null;
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			
			//terminal could be a modifier chunk or a modifier chunk stuck into another chunk (e.g. character_state)
			if(previousModifierChunk != null && terminalChunk.isPartOfChunkType(terminal, ChunkType.MODIFIER) &&
					!terminalChunk.getChunkOfTypeAndTerminal(ChunkType.MODIFIER, terminal).equals(previousModifierChunk)) {
				Chunk modifierChunk = terminalChunk.getChunkOfTypeAndTerminal(ChunkType.MODIFIER, terminal);
				LinkedHashSet<Chunk> previousChildChunks = previousModifierChunk.getChunks();
				LinkedHashSet<Chunk> modifierChildChunks = modifierChunk.getChunks();
				previousChildChunks.addAll(modifierChildChunks);
				Chunk newModifierChunk = new Chunk(ChunkType.MODIFIER, previousChildChunks);
				terminalChunk.replaceChunk(modifierChunk, newModifierChunk);
				terminalChunk.removeChunk(previousModifierChunk);
				chunkCollector.addChunk(terminalChunk);
			}
			
			//special case: to connection two modifiers e.g. broadly to narrowly ovoid
			if(terminal.getTerminalsText().equals("to") && 
					!chunkCollector.isPartOfANonTerminalChunk(terminal) &&
					previousModifierChunk != null && 
					i+1 < terminals.size() && 
					chunkCollector.isPartOfChunkType(terminals.get(i+1), ChunkType.MODIFIER)) {
				previousModifierChunk.getChunks().add(terminal);
			} else 
				previousModifierChunk = terminalChunk.getChunkOfTypeAndTerminal(ChunkType.MODIFIER, terminal);
		}

		//moveChunksForAndOrLists(terminals, chunkCollector);		
	}

	private void determineTranslationsBasedOnStructure(List<AbstractParseTree> terminals, ChunkCollector chunkCollector,
			boolean[] translateCharacterToConstraint, boolean[] translateStateToConstraint, boolean[] translateModifierToConstraint, 
			boolean[] translateStateToModifier, boolean[] translateConstraintToModifier) {
		boolean[] modifier = getArray(terminals, chunkCollector, ChunkType.MODIFIER);
		boolean[] character = getArray(terminals, chunkCollector, ChunkType.CHARACTER_STATE);
		boolean[] state = getArray(terminals, chunkCollector, ChunkType.STATE, ChunkType.VERB);
		boolean[] organ = getArray(terminals, chunkCollector, ChunkType.ORGAN);
		boolean[] constraint = getArray(terminals, chunkCollector, ChunkType.CONSTRAINT);
		boolean[] constraintOrgan = new boolean[terminals.size()];
		
		boolean changed = true;
		while(changed) {
			changed = false;
			
			boolean changedConstraintOrgan = false;
			for(int i=terminals.size() - 1; i>=0; i--) {
				boolean before = constraintOrgan[i];
				constraintOrgan[i] = organ[i] || (i+1 < terminals.size() && constraint[i] && organ[i+1]); 
				changedConstraintOrgan |= constraintOrgan[i] != before;
			}
			//log(LogLevel.DEBUG, "changedConstraintOrgan " + changedConstraintOrgan);
			changed |= changedConstraintOrgan;
			
			boolean changedCharacterToConstraint = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateCharacterToConstraint[i];
				translateCharacterToConstraint[i] = character[i] && i+1 < terminals.size() && 
					(constraintOrgan[i+1] || translateCharacterToConstraint[i+1]) && ((i-1 >= 0 && !modifier[i-1]) || i==0);
				changedCharacterToConstraint |= translateCharacterToConstraint[i] != before;
			}
			//log(LogLevel.DEBUG, "changedCharacterToConstraint " + changedCharacterToConstraint);
			changed |= changedCharacterToConstraint;
			
			boolean changedStateToX = false;
			for(int i=terminals.size()-1; i>=0; i--) 
				if(state[i] && !character[i]) {
					boolean before = translateStateToConstraint[i];
					translateStateToConstraint[i] = i+1 < terminals.size() && (constraintOrgan[i+1] || translateCharacterToConstraint[i+1]);
					changedStateToX |= translateStateToConstraint[i] != before;
					before = translateStateToModifier[i];
					translateStateToModifier[i] =  i+1 < terminals.size() && character[i+1] && !translateCharacterToConstraint[i+1] && 
							!terminals.get(i).getTerminalsText().equals("borne");
					changedStateToX |= translateStateToModifier[i] != before;
				}
			//log(LogLevel.DEBUG, "changedStateToX " + changedStateToX);
			changed |= changedStateToX;
			
			boolean changedModifierToConstraint = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateModifierToConstraint[i];
				translateModifierToConstraint[i] = (modifier[i] || translateStateToModifier[i]) && i+1 < terminals.size() && (constraintOrgan[i+1] || translateCharacterToConstraint[i+1] ||
						translateStateToConstraint[i+1]);
				changedModifierToConstraint |= translateModifierToConstraint[i] != before;
			}
			//log(LogLevel.DEBUG, "changedModifierToConstraint " + changedModifierToConstraint);
			changed |= changedModifierToConstraint;
			
			boolean changedConstraintToModifier = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateConstraintToModifier[i];
				translateConstraintToModifier[i] = (constraint[i] || translateCharacterToConstraint[i] || translateStateToConstraint[i] || translateModifierToConstraint[i]) && 
					i+1 < terminals.size() && character[i+1] && !translateCharacterToConstraint[i+1];
				changedConstraintToModifier |= translateConstraintToModifier[i] != before;
			}
			//log(LogLevel.DEBUG, "changedConstraintToModifier " + changedConstraintToModifier);
			changed |= changedConstraintToModifier;
		}
	}

	private boolean characterPermitsTranslation(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		if(i+1<terminals.size()) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk chunk = chunkCollector.getChunk(terminal);
			Chunk characterStateChunk = chunk.getChunkOfTypeAndTerminal(ChunkType.CHARACTER_STATE, terminal);
			
			String characterName = characterStateChunk.getProperty("characterName");
			String characterState = characterStateChunk.getChunkBFS(ChunkType.STATE).getTerminalsText();
			
			HashSet<String> allowedCharacterNames = new HashSet<String>();
			String[] list = ElementRelationGroup.entityConstraintElements.split("\\|");
			for(String cat: list){
				allowedCharacterNames.add(cat);
			}
			/*allowedCharacterNames.add("position");
			allowedCharacterNames.add("insertion");
			allowedCharacterNames.add("structure_type");
			allowedCharacterNames.add("structure_subtype");
			allowedCharacterNames.add("structure_in_adjective_form");
			allowedCharacterNames.add("function");
			allowedCharacterNames.add("growth_order");*/
			
			HashSet<String> notAllowedCharacterStates = new HashSet<String>();
			notAllowedCharacterStates.add("low");
			
			//split character name by "_" ?
			
			if(characterName != null) {
				/*String[] singleCharacterNames = characterName.split("_or_");
				for(String singleCharacterName : singleCharacterNames) {
					if(singleCharacterName.equals("size") && (characterState.endsWith("est") || characterState.endsWith("er")))
						return true;
				}*/
				if(characterName.matches(".*?(^|_)(size|length|width|height|thickness)(_|$).*") && (characterState.endsWith("est") || characterState.endsWith("er")))
					return true;
				
				String[] characterNames = characterName.split("_or_");
				if(notAllowedCharacterStates.contains(characterState)) return false;
				boolean result = false;
				for(String singleCharacterName : characterNames)
					result |= allowedCharacterNames.contains(singleCharacterName);
					//result &= allowedCharacterNames.contains(singleCharacterName);

				return result;
			}
		}
		return false;
	}
	
	private void translate(ChunkType oldChunkType, ChunkType newChunkType, int i, ChunkCollector chunkCollector) {
		AbstractParseTree terminal = chunkCollector.getTerminals().get(i);
		Chunk parentChunk = chunkCollector.getChunk(terminal);
		Chunk chunk = parentChunk.getChunkOfTypeAndTerminal(oldChunkType, terminal);
		//if(oldChunkType.equals(ChunkType.CHARACTER_STATE))
		//	chunk.clearProperties();
		if(chunk!=null) chunk.setChunkType(newChunkType);
	}	
	
	
	
	
	private boolean[] getArray(List<AbstractParseTree> terminals, ChunkCollector chunkCollector, ChunkType chunkType) {
		boolean[] result = new boolean[terminals.size()];
		for(int i=terminals.size() - 1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, chunkType)) {
				result[i] = true;
			}
		}
		return result;
	}
	
	
	private boolean[] getArray(List<AbstractParseTree> terminals, ChunkCollector chunkCollector, ChunkType chunkType, ChunkType notChunkType) {
		boolean[] result = new boolean[terminals.size()];
		for(int i=terminals.size() - 1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, chunkType) && !terminalChunk.isPartOfChunkType(terminal, notChunkType)) {
				result[i] = true;
			}
		}
		return result;
	}
}
