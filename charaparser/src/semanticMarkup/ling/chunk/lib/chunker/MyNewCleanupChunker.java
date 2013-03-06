package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MyNewCleanupChunker extends AbstractChunker {

	@Inject
	public MyNewCleanupChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, inflector);
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
				if(characterPermitsTranslation(i, terminals, chunkCollector))
					translate(ChunkType.CHARACTER_STATE, ChunkType.CONSTRAINT, i, chunkCollector);
			if(translateStateToConstraint[i])
				translate(ChunkType.STATE, ChunkType.CONSTRAINT, i, chunkCollector);
			if(translateModifierToConstraint[i])
				translate(ChunkType.MODIFIER, ChunkType.CONSTRAINT, i, chunkCollector);
			if(translateStateToModifier[i])
				translate(ChunkType.STATE, ChunkType.MODIFIER, i, chunkCollector);
			if(translateConstraintToModifier[i])
				translate(ChunkType.CONSTRAINT, ChunkType.MODIFIER, i, chunkCollector);
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
			log(LogLevel.DEBUG, "changedConstraintOrgan " + changedConstraintOrgan);
			changed |= changedConstraintOrgan;
			
			boolean changedCharacterToConstraint = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateCharacterToConstraint[i];
				translateCharacterToConstraint[i] = character[i] && i+1 < terminals.size() && 
					(constraintOrgan[i+1] || translateCharacterToConstraint[i+1]) && ((i-1 >= 0 && !modifier[i-1]) || i==0);
				changedCharacterToConstraint |= translateCharacterToConstraint[i] != before;
			}
			log(LogLevel.DEBUG, "changedCharacterToConstraint " + changedCharacterToConstraint);
			changed |= changedCharacterToConstraint;
			
			boolean changedStateToX = false;
			for(int i=terminals.size()-1; i>=0; i--) 
				if(state[i] && !character[i]) {
					boolean before = translateStateToConstraint[i];
					translateStateToConstraint[i] = i+1 < terminals.size() && (constraintOrgan[i+1] || translateCharacterToConstraint[i+1]);
					changedStateToX |= translateStateToConstraint[i] != before;
					before = translateStateToModifier[i];
					translateStateToModifier[i] =  i+1 < terminals.size() && character[i+1] && !translateCharacterToConstraint[i+1];
					changedStateToX |= translateStateToModifier[i] != before;
				}
			log(LogLevel.DEBUG, "changedStateToX " + changedStateToX);
			changed |= changedStateToX;
			
			boolean changedModifierToConstraint = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateModifierToConstraint[i];
				translateModifierToConstraint[i] = (modifier[i] || translateStateToModifier[i]) && i+1 < terminals.size() && (constraintOrgan[i+1] || translateCharacterToConstraint[i+1] ||
						translateStateToConstraint[i+1]);
				changedModifierToConstraint |= translateModifierToConstraint[i] != before;
			}
			log(LogLevel.DEBUG, "changedModifierToConstraint " + changedModifierToConstraint);
			changed |= changedModifierToConstraint;
			
			boolean changedConstraintToModifier = false;
			for(int i=terminals.size()-1; i>=0; i--) {
				boolean before = translateConstraintToModifier[i];
				translateConstraintToModifier[i] = (constraint[i] || translateCharacterToConstraint[i] || translateStateToConstraint[i] || translateModifierToConstraint[i]) && 
					i+1 < terminals.size() && character[i+1] && !translateCharacterToConstraint[i+1];
				changedConstraintToModifier |= translateConstraintToModifier[i] != before;
			}
			log(LogLevel.DEBUG, "changedConstraintToModifier " + changedConstraintToModifier);
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
			allowedCharacterNames.add("position");
			allowedCharacterNames.add("insertion");
			allowedCharacterNames.add("structure_type");
			
			HashSet<String> notAllowedCharacterStates = new HashSet<String>();
			notAllowedCharacterStates.add("low");
			
			//split character name by "_" ?
			if(characterName != null) {
				String[] singleCharacterNames = characterName.split("_");
				for(String singleCharacterName : singleCharacterNames) {
					if(allowedCharacterNames.contains(singleCharacterName) && !notAllowedCharacterStates.contains(characterState))
						return true;
				}
			}
				
			//split character name with "_" instead of string contains
			if(characterName != null) {
				String[] singleCharacterNames = characterName.split("_");
				for(String singleCharacterName : singleCharacterNames) {
					if(singleCharacterName.equals("size") && (characterState.endsWith("est") || characterState.endsWith("er")))
						return true;
				}
			}
		}
		return false;
	}
	
	private void translate(ChunkType oldChunkType, ChunkType newChunkType, int i, ChunkCollector chunkCollector) {
		AbstractParseTree terminal = chunkCollector.getTerminals().get(i);
		Chunk parentChunk = chunkCollector.getChunk(terminal);
		Chunk chunk = parentChunk.getChunkOfTypeAndTerminal(oldChunkType, terminal);
		chunk.setChunkType(newChunkType);
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
	
	
	
//	private void moveChunksForAndOrLists(List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
//		for(int i=terminals.size()-1; i>=0; i--) {
//			AbstractParseTree terminal = terminals.get(i);
//		
//			/**
//			 * treat the case where terminal is 'and' or 'or'
//			 */
//			if(terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or")) {
//				if(i-1>=0 && i+1<terminals.size()) {
//					AbstractParseTree previousTerminal = terminals.get(i-1);
//					AbstractParseTree nextTerminal = terminals.get(i+1);
//					
//					/**
//					 * check the chunks of previous and next terminal
//					 */
//					//outer and middle phyllary
//					boolean previousConstraint = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT);
//					boolean previousModifier = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER);
//					boolean nextConstraint = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT);
//					boolean nextModifier = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER);
//					
//					/**
//					 * previously and next a modifying/constraining thing
//					 */
//					if((previousConstraint || previousModifier) && (nextConstraint || nextModifier)) {
//					/*if((chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT) || 
//							chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER) &&
//							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) || 
//							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER)) {*/
//						
//						/**
//						 * assumption that previous, and/or, nextchunk already in one parentchunk together
//						 */
//						Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
//						List<Chunk> modifierChunks = chunkParent.getChunks(ChunkType.MODIFIER);
//	
//						/**
//						 * if nextTerminal is contained in a modifier chunk then remove previous terminal and and/or from the parent
//						 * and instead stick them into the modifier chunk of next terminal
//						 */
//						for(Chunk modifierChunk : modifierChunks) {
//							if(modifierChunk.contains(nextTerminal)) {
//								chunkParent.removeChunk(previousTerminal);
//								chunkParent.removeChunk(terminal);
//								
//								LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
//								newChunks.add(previousTerminal);
//								newChunks.add(terminal);
//								newChunks.addAll(modifierChunk.getChunks());
//								modifierChunk.setChunks(newChunks);
//							}
//						}
//						
//						/**
//						 * if nextTerminal is contained in a constraint chunk then remove previous terminal and and/or from the parent
//						 * and instead stick them into the constraint chunk of next terminal
//						 */
//						List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
//						for(Chunk constraintChunk : constraintChunks) {
//							if(constraintChunk.contains(nextTerminal)) {
//								chunkParent.removeChunk(previousTerminal);
//								chunkParent.removeChunk(terminal);
//								
//								LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
//								newChunks.add(previousTerminal);
//								newChunks.add(terminal);
//								newChunks.addAll(constraintChunk.getChunks());
//								constraintChunk.setChunks(newChunks);
//							}
//						}
//			
//						chunkCollector.addChunk(chunkParent);
//					
//					/**
//					* !previously and next a modifying/constraining thing
//					*/
//					} else {
//					
//						//outer phyllary and middle phyllary
//						
//						/**
//						 * next terminal still part of constraint
//						 */
//						if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) {
//							Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
//							List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
//							
//							for(Chunk constraintChunk : constraintChunks) {
//								if(constraintChunk.contains(nextTerminal)) {
//									/**
//									 * get the previous and next organ chunk from there on
//									 */
//									Chunk previousOrganChunk = getPreviousOrganChunk(i, terminals, chunkCollector);
//									Chunk nextOrganChunk = getNextOrganChunk(i, terminals, chunkCollector);
//									
//									/**
//									 * if found in both directions and both organs are the same stick the and/or terminal into the chunk parent of next terminal (?)
//									 */
//									if(previousOrganChunk != null && nextOrganChunk != null && 
//											previousOrganChunk.getTerminalsText().equals(nextOrganChunk.getTerminalsText())) {
//										chunkParent.removeChunk(terminal);
//										
//										LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
//										newChunks.add(terminal);
//										newChunks.addAll(constraintChunk.getChunks());
//										constraintChunk.setChunks(newChunks);
//									}
//								}
//							}
//							chunkCollector.addChunk(chunkParent);
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	
//	/**
//	 * @param i
//	 * @param terminals
//	 * @param chunkCollector
//	 * @return the next organ chunk from i onwards
//	 */
//	private Chunk getNextOrganChunk(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
//		for(; i<terminals.size(); i++) {
//			AbstractParseTree nextTerminal = terminals.get(i);
//			Chunk nextChunk = chunkCollector.getChunk(nextTerminal);
//			Chunk organChunk = nextChunk.getChunkDFS(ChunkType.ORGAN);
//			if(organChunk!=null) 
//				return organChunk;
//		}
//		return null;
//	}
//
//	/**
//	 * @param i
//	 * @param terminals
//	 * @param chunkCollector
//	 * @return the previous organ chunk from i backwards
//	 */
//	private Chunk getPreviousOrganChunk(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
//		for(; i>=0; i--) {
//			AbstractParseTree previousTerminal = terminals.get(i);
//			Chunk nextChunk = chunkCollector.getChunk(previousTerminal);
//			Chunk organChunk = nextChunk.getChunkDFS(ChunkType.ORGAN);
//			if(organChunk!=null) 
//				return organChunk;
//		}
//		return null;
//	}
	
}
