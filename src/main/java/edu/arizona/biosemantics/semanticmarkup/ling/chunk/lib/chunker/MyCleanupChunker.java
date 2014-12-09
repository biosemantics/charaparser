package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
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
 * not being used
 * MyCleanupChunker reformats the chunks after all other chunkers have been run to obtain an overall consistent chunking result
 * Specifically modifier, character, constraint and organ chunks are reconsidered
 * @author rodenhausen
 */
public class MyCleanupChunker extends AbstractChunker {

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
	public MyCleanupChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector, learnedCharacterKnowledgeBase);
	}

	/**

	 */
	/**
	 * 
	 * @param terminals
	 * @param chunkCollector
	 * @return boolean array which specifies whether a character can be translated to a constraint based on the
	 * following factors:
	 * character directly preceeds a (constraint) organ chain AND is the character itself is not preceeded by a modifier
	 * this definition extends recursively for all possible character predecessors of such a chain
	 */
	private boolean[] getTranslateCharacterToConstraintArray(List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {

		boolean[] modifier = new boolean[terminals.size()];
		for(int i=terminals.size() - 1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, ChunkType.MODIFIER)) {
				modifier[i] = true;
			}
		}
		
		boolean[] character = new boolean[terminals.size()];
		for(int i=terminals.size() - 1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, ChunkType.STATE)) {
				character[i] = true;
			}
		}
		
		boolean[] organ = new boolean[terminals.size()];
		for(int i=terminals.size() - 1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				organ[i] = true;
			}
			if(terminalChunk.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) && i+1 < terminals.size() && organ[i+1]) {
				organ[i] = true;
			}
		}
		
		boolean[] result = new boolean[terminals.size()];
		for(int i=terminals.size()-1; i>=0; i--)
			result[i] = character[i] && i+1 < terminals.size() && (organ[i+1] || result[i+1]) && ((i-1 >= 0 && !modifier[i-1]) || i==0);
		
		return result;
	}

	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		
		boolean[] translateCharacterToConstraintArray = getTranslateCharacterToConstraintArray(terminals, chunkCollector);
		
		boolean previousTerminalOrgan = false;
		boolean previousTerminalState = false;
		boolean previousTerminalConstraint = false;
		
		/**
		 * from back to front 
		 */
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			
			/**
			 * check if character and that character can be translated into constraint according to array
			 * if so translate character for position characters
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) && translateCharacterToConstraintArray[i]) {
				if(translateCharacterToConstraintForPosition(i, terminals, chunkCollector)) {
					previousTerminalOrgan = true;
					previousTerminalState = false;
					continue;
				}
			}
			
			/**
			 * check if character and prev terminal constraint and array allows translation
			 * -> translate character to constraint
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) && 
					previousTerminalConstraint && translateCharacterToConstraintArray[i]) {
				Chunk chunk = chunkCollector.getChunk(terminal);
				Chunk characterStateChunk = chunk.getChunkOfTypeAndTerminal(ChunkType.CHARACTER_STATE, terminal);
				characterStateChunk.clearProperties();
				characterStateChunk.clearChunks();
				LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
				chunks.add(terminal);
				characterStateChunk.setChunkType(ChunkType.CONSTRAINT);
				characterStateChunk.setChunks(chunks);
				chunkCollector.addChunk(chunk);
			}
			
			/**
			 * if terminal part of a state but not a character chunk then 
			 * -> translate to constranit if previous organ
			 * -> translate to modifier if previously state/character
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) {
				Chunk stateParentChunk = chunkCollector.getChunk(terminal);
				Chunk stateChunk = stateParentChunk.getChunkOfTypeAndTerminal(ChunkType.STATE, terminal);
				if(previousTerminalOrgan)
					stateChunk.setChunkType(ChunkType.CONSTRAINT);
				else if(previousTerminalState)
					stateChunk.setChunkType(ChunkType.MODIFIER);
				chunkCollector.addChunk(stateParentChunk);
			}
			
			/**
			 * if terminal part of modifier and previously organ
			 * -> translate modifier to constraint
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.MODIFIER) && previousTerminalOrgan){
				Chunk modifierChunkParent = chunkCollector.getChunk(terminal);
				List<Chunk> modifierChunks = modifierChunkParent.getChunks(ChunkType.MODIFIER);
				for(Chunk modifierChunk : modifierChunks) {
					if(modifierChunk.containsOrEquals(terminal)) {
						modifierChunk.setChunkType(ChunkType.CONSTRAINT);
					}
				}
				//modifierChunk.setChunkType(ChunkType.CONSTRAINT);
				chunkCollector.addChunk(modifierChunkParent);
			}
			
			/**
			 * terminal part of constraint and previously state/character
			 * -> translate to modifier
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) && previousTerminalState) {
				Chunk constraintChunkParent = chunkCollector.getChunk(terminal);
				List<Chunk> constraintChunks = constraintChunkParent.getChunks(ChunkType.CONSTRAINT);
				for(Chunk constraintChunk : constraintChunks) {
					if(constraintChunk.containsOrEquals(terminal)) {
						constraintChunk.setChunkType(ChunkType.MODIFIER);
					}
				}
				//chunkCollector.getChunk(terminal).setChunkType(ChunkType.MODIFIER);
				chunkCollector.addChunk(constraintChunkParent);
			}
			
			
			/**
			 *	if current terminal part of organ/constraint or charater/modifier/state then set bools accordingly for next round 
			 */
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) || chunkCollector.isPartOfChunkType(terminal, ChunkType.NON_SUBJECT_ORGAN) 
					|| chunkCollector.isPartOfChunkType(terminal, ChunkType.MAIN_SUBJECT_ORGAN) || 
					(previousTerminalOrgan && chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT))) {
				previousTerminalOrgan = true;
			} else {
				previousTerminalOrgan = false;
			}
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) || chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE) 
					|| (previousTerminalState && chunkCollector.isPartOfChunkType(terminal, ChunkType.MODIFIER))) {
				previousTerminalState = true;
			} else {
				previousTerminalState = false;
			}
			
			previousTerminalConstraint = chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT);
		}
		
		
		/**
		 * from back to front 
		 */
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			
			/**
			 * treat the case where terminal is 'and' or 'or'
			 */
			if(terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or")) {
				if(i-1>=0 && i+1<terminals.size()) {
					AbstractParseTree previousTerminal = terminals.get(i-1);
					AbstractParseTree nextTerminal = terminals.get(i+1);
					
					/**
					 * check the chunks of previous and next terminal
					 */
					//outer and middle phyllary
					boolean previousConstraint = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT);
					boolean previousModifier = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER);
					boolean nextConstraint = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT);
					boolean nextModifier = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER);
					
					/**
					 * previously and next a modifying/constraining thing
					 */
					if((previousConstraint || previousModifier) && (nextConstraint || nextModifier)) {
					/*if((chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT) || 
							chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER) &&
							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) || 
							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER)) {*/
						
						/**
						 * assumption that previous, and/or, nextchunk already in one parentchunk together
						 */
						Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
						List<Chunk> modifierChunks = chunkParent.getChunks(ChunkType.MODIFIER);
	
						/**
						 * if nextTerminal is contained in a modifier chunk then remove previous terminal and and/or from the parent
						 * and instead stick them into the modifier chunk of next terminal
						 */
						for(Chunk modifierChunk : modifierChunks) {
							if(modifierChunk.containsOrEquals(nextTerminal)) {
								chunkParent.removeChunk(previousTerminal);
								chunkParent.removeChunk(terminal);
								
								LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
								newChunks.add(previousTerminal);
								newChunks.add(terminal);
								newChunks.addAll(modifierChunk.getChunks());
								modifierChunk.setChunks(newChunks);
							}
						}
						
						/**
						 * if nextTerminal is contained in a constraint chunk then remove previous terminal and and/or from the parent
						 * and instead stick them into the constraint chunk of next terminal
						 */
						List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
						for(Chunk constraintChunk : constraintChunks) {
							if(constraintChunk.containsOrEquals(nextTerminal)) {
								chunkParent.removeChunk(previousTerminal);
								chunkParent.removeChunk(terminal);
								
								LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
								newChunks.add(previousTerminal);
								newChunks.add(terminal);
								newChunks.addAll(constraintChunk.getChunks());
								constraintChunk.setChunks(newChunks);
							}
						}
			
						chunkCollector.addChunk(chunkParent);
					
					/**
					* !previously and next a modifying/constraining thing
					*/
					} else {
					
						//outer phyllary and middle phyllary
						
						/**
						 * next terminal still part of constraint
						 */
						if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) {
							Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
							List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
							
							for(Chunk constraintChunk : constraintChunks) {
								if(constraintChunk.containsOrEquals(nextTerminal)) {
									/**
									 * get the previous and next organ chunk from there on
									 */
									Chunk previousOrganChunk = getPreviousOrganChunk(i, terminals, chunkCollector);
									Chunk nextOrganChunk = getNextOrganChunk(i, terminals, chunkCollector);
									
									/**
									 * if found in both directions and both organs are the same stick the and/or terminal into the chunk parent of next terminal (?)
									 */
									if(previousOrganChunk != null && nextOrganChunk != null && 
											previousOrganChunk.getTerminalsText().equals(nextOrganChunk.getTerminalsText())) {
										chunkParent.removeChunk(terminal);
										
										LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
										newChunks.add(terminal);
										newChunks.addAll(constraintChunk.getChunks());
										constraintChunk.setChunks(newChunks);
									}
								}
							}
							chunkCollector.addChunk(chunkParent);
						}
					}
				}
			}
		}
	}

	/**
	 * is not being used
	 * translates character to constraint if character state of certain value or charactername of certain type
	 * or if next terminal is organ and previous characters are of comparison type
	 * @param i
	 * @param terminals
	 * @param chunkCollector
	 * @return true if translation has been done
	 */
	private boolean translateCharacterToConstraintForPosition(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		if(i+1<terminals.size()) {
			AbstractParseTree terminal = terminals.get(i);
			
			Chunk chunk = chunkCollector.getChunk(terminal);
			//log(LogLevel.DEBUG, chunk);
			List<Chunk> characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
			boolean hasChanged = false;
			
			/**
			 * for all characterstate chunks at terminal i position
			 */
			for(Chunk characterStateChunk : characterStateChunks) {
				String character = characterStateChunk.getProperty("characterName");
				String characterState = characterStateChunk.getChunkBFS(ChunkType.STATE).getTerminalsText();
				
				/**
				 * if the characterstate is not low and (tags or modifiers or the character is of the certain types 
				 * translate character to constraint
				 */
				//if(character!=null && !characterState.equals("low") &&
				//		(character.contains("position") || character.contains("insertion") || character.contains("structure_type") || character.contains("structure_subtype") 
				//				|| character.contains("structure_in_adjective_form") || character.contains("function") || character.contains("growth_order") ||
				//				this.terminologyLearner.getTags().contains(characterState) || this.terminologyLearner.getModifiers().contains(characterState))
				//		) {

				if(character!=null && !characterState.equals("low") && (
						character.matches(".*?(^|_or_)("+ElementRelationGroup.entityConstraintElements+")(_or_|$).*") ||
						this.terminologyLearner.getTags().contains(characterState) || this.terminologyLearner.getModifiers().contains(characterState))
						) {
				
					characterStateChunk.setChunks(new LinkedHashSet<Chunk>(characterStateChunk.getTerminals()));
					characterStateChunk.setChunkType(ChunkType.CONSTRAINT);
					characterStateChunk.clearProperties();
					hasChanged = true;
				} 
			}
			if(hasChanged) {
				chunkCollector.addChunk(chunk);
				return true;
			}
			
			/**
			 * if the nextterminal is an organ and previously there are characterstates
			 */
			AbstractParseTree nextTerminal = terminals.get(i+1);
			if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.ORGAN)) {
				chunk = chunkCollector.getChunk(terminal);
				characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
				hasChanged = false;
				for(Chunk characterStateChunk : characterStateChunks) {
					Chunk stateChunk = characterStateChunk.getChunkBFS(ChunkType.STATE);
					String character = characterStateChunk.getProperty("characterName");
					
					/**
					 * if the characters is of size and the value is of a comparison kind (-er/-est)
					 * translate to constraint
					 */
					if(character != null && character.contains("size") && (stateChunk.getTerminalsText().endsWith("est") || stateChunk.getTerminalsText().endsWith("er"))) {
						characterStateChunk.setChunks(new LinkedHashSet<Chunk>(characterStateChunk.getTerminals()));
						characterStateChunk.setChunkType(ChunkType.CONSTRAINT);
						characterStateChunk.clearProperties();
						hasChanged = true;
					} 
				}
				if(hasChanged) {
					chunkCollector.addChunk(chunk);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param i
	 * @param terminals
	 * @param chunkCollector
	 * @return the next organ chunk from i onwards
	 */
	private Chunk getNextOrganChunk(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		for(; i<terminals.size(); i++) {
			AbstractParseTree nextTerminal = terminals.get(i);
			Chunk nextChunk = chunkCollector.getChunk(nextTerminal);
			Chunk organChunk = nextChunk.getChunkDFS(ChunkType.ORGAN);
			if(organChunk!=null) 
				return organChunk;
		}
		return null;
	}

	/**
	 * @param i
	 * @param terminals
	 * @param chunkCollector
	 * @return the previous organ chunk from i backwards
	 */
	private Chunk getPreviousOrganChunk(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		for(; i>=0; i--) {
			AbstractParseTree previousTerminal = terminals.get(i);
			Chunk nextChunk = chunkCollector.getChunk(previousTerminal);
			Chunk organChunk = nextChunk.getChunkDFS(ChunkType.ORGAN);
			if(organChunk!=null) 
				return organChunk;
		}
		return null;
	}
}
