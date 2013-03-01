package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MyCleanupChunker extends AbstractChunker {

	private IOrganStateKnowledgeBase organStateKnowledgeBase;

	@Inject
	public MyCleanupChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, inflector);
		this.organStateKnowledgeBase = organStateKnowledgeBase;
	}

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
		
		
		/*boolean seenOtherThanOrganOrConstraint = false;
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			result[i] = true;
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && !seenOtherThanOrganOrConstraint) {
				result[i] = false;
			}
			if(!terminalChunk.isPartOfChunkType(terminal, ChunkType.ORGAN) && !terminalChunk.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) && 
					!terminalChunk.isPartOfChunkType(terminal, ChunkType.UNASSIGNED) && !terminalChunk.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE))
				seenOtherThanOrganOrConstraint = true;
		}*/
		
		/*boolean previousOrganOrConstraint = false;
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isPartOfChunkType(terminal, ChunkType.STATE)) {
				if(previousOrganOrConstraint) {
					result[i] = true;
				}
			} else {
				previousOrganOrConstraint = terminalChunk.isPartOfChunkType(terminal, ChunkType.ORGAN) || 
						(terminalChunk.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) && previousOrganOrConstraint);
			}
		}*/
		
		return result;
	}

	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		
		boolean[] translateCharacterToConstraintArray = getTranslateCharacterToConstraintArray(terminals, chunkCollector);
		
		boolean previousTerminalOrgan = false;
		boolean previousTerminalState = false;
		boolean previousTerminalConstraint = false;
		
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) && translateCharacterToConstraintArray[i]) {
				if(translateCharacterToConstraintForPosition(i, terminals, chunkCollector)) {
					previousTerminalOrgan = true;
					previousTerminalState = false;
					continue;
				}
			}
			
			
			/*
			if(previousTerminalOrgan) {
				if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) {
					Chunk stateParentChunk = chunkCollector.getChunk(terminal);
					Chunk stateChunk = stateParentChunk.getChunkBFS(ChunkType.CHARACTER_STATE);
					stateChunk.clearChunks();
					stateChunk.clearProperties();
					LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
					chunks.add(terminal);
					stateChunk.setChunks(chunks);
					stateChunk.setChunkType(ChunkType.CONSTRAINT);
					chunkCollector.addChunk(stateParentChunk);
				} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE)) {
					Chunk stateParentChunk = chunkCollector.getChunk(terminal);
					Chunk stateChunk = stateParentChunk.getChunkBFS(ChunkType.STATE);
					stateChunk.setChunkType(ChunkType.CONSTRAINT);
					chunkCollector.addChunk(stateParentChunk);
				}
			}*/
			
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
			
			/*
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE) && 
				!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) {
				Chunk stateParentChunk = chunkCollector.getChunk(terminal);
				Chunk stateChunk = stateParentChunk.getChunkOfTypeAndTerminal(ChunkType.STATE, terminal);
				if(previousTerminalState) {
					stateChunk.setChunkType(ChunkType.MODIFIER);
					chunkCollector.addChunk(stateParentChunk);
				}
			}
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE) && 
					(chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) || 
							!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE))) { 
					//{// && 
					//!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) {
				Chunk stateParentChunk = chunkCollector.getChunk(terminal);
				Chunk stateChunk = stateParentChunk.getChunkOfTypeAndTerminal(ChunkType.STATE, terminal);
				if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) { 
					stateChunk = stateParentChunk.getChunkOfTypeAndTerminal(ChunkType.CHARACTER_STATE, terminal);
				}
				if(previousTerminalOrgan) {
					stateChunk.clearProperties();
					stateChunk.clearChunks();
					
					LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
					chunks.add(terminal);
					stateChunk.setChunks(chunks);
					stateChunk.setChunkType(ChunkType.CONSTRAINT);
					chunkCollector.addChunk(stateParentChunk);
				}
			}*/
	
			/*if(!chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				if(previousTerminalOrgan) {
					Chunk newChunk = new Chunk(ChunkType.CONSTRAINT, terminal);
					chunkCollector.addChunk(newChunk);
				}
				if(previousTerminalState) {
					Chunk newChunk = new Chunk(ChunkType.MODIFIER, terminal);
					chunkCollector.addChunk(newChunk);
				}
			}*/
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.MODIFIER) && previousTerminalOrgan){
				Chunk modifierChunkParent = chunkCollector.getChunk(terminal);
				List<Chunk> modifierChunks = modifierChunkParent.getChunks(ChunkType.MODIFIER);
				for(Chunk modifierChunk : modifierChunks) {
					if(modifierChunk.contains(terminal)) {
						modifierChunk.setChunkType(ChunkType.CONSTRAINT);
					}
				}
				//modifierChunk.setChunkType(ChunkType.CONSTRAINT);
				chunkCollector.addChunk(modifierChunkParent);
			}
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) && previousTerminalState) {
				Chunk constraintChunkParent = chunkCollector.getChunk(terminal);
				List<Chunk> constraintChunks = constraintChunkParent.getChunks(ChunkType.CONSTRAINT);
				for(Chunk constraintChunk : constraintChunks) {
					if(constraintChunk.contains(terminal)) {
						constraintChunk.setChunkType(ChunkType.MODIFIER);
					}
				}
				//chunkCollector.getChunk(terminal).setChunkType(ChunkType.MODIFIER);
				chunkCollector.addChunk(constraintChunkParent);
			}
			
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
		
		/*for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				modifierConstraintCandidates.add(terminal);
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) || chunkCollector.isPartOfChunkType(terminal, ChunkType.NON_SUBJECT_ORGAN) ) 
				|| chunkCollector.isPartOfChunkType(terminal, ChunkType.MAIN_SUBJECT_ORGAN)) {
					
				Chunk organChunk = chunkCollector.getChunk(terminal);
				LinkedHashSet<Chunk> childChunks = organChunk.getChunks();
				LinkedHashSet<Chunk> newChildChunks = new LinkedHashSet<Chunk>();
					
				for(AbstractParseTree modifierConstraintCandidate : modifierConstraintCandidates) {
					newChildChunks.add(new Chunk(ChunkType.CONSTRAINT, modifierConstraintCandidate));
				}
				
				newChildChunks.addAll(childChunks);
				organChunk.setChunks(newChildChunks);
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) || chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE)) {
				
				Chunk organChunk = chunkCollector.getChunk(terminal);
				LinkedHashSet<Chunk> childChunks = organChunk.getChunks();
				LinkedHashSet<Chunk> newChildChunks = new LinkedHashSet<Chunk>();
					
				for(AbstractParseTree modifierConstraintCandidate : modifierConstraintCandidates) {
					newChildChunks.add(new Chunk(ChunkType.CONSTRAINT, modifierConstraintCandidate));
				}
				
				newChildChunks.addAll(childChunks);
				organChunk.setChunks(newChildChunks);
				
			} else if(chunkCollector.isPar){
				modifierConstraintCandidates.clear();
			}
		}*/
		
		
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or")) {
				if(i-1>=0 && i+1<terminals.size()) {
					AbstractParseTree previousTerminal = terminals.get(i-1);
					AbstractParseTree nextTerminal = terminals.get(i+1);
					
					//outer and middle phyllary
					boolean previousConstraint = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT);
					boolean previousModifier = chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER);
					boolean nextConstraint = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT);
					boolean nextModifier = chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER);
					if((previousConstraint || previousModifier) && (nextConstraint || nextModifier)) {
					/*if((chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.CONSTRAINT) || 
							chunkCollector.isPartOfChunkType(previousTerminal, ChunkType.MODIFIER) &&
							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) || 
							chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MODIFIER)) {*/
						Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
						
						List<Chunk> modifierChunks = chunkParent.getChunks(ChunkType.MODIFIER);
	
						for(Chunk modifierChunk : modifierChunks) {
							if(modifierChunk.contains(nextTerminal)) {
								chunkParent.removeChunk(previousTerminal);
								chunkParent.removeChunk(terminal);
								
								LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
								newChunks.add(previousTerminal);
								newChunks.add(terminal);
								newChunks.addAll(modifierChunk.getChunks());
								modifierChunk.setChunks(newChunks);
							}
						}
						
						List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
						for(Chunk constraintChunk : constraintChunks) {
							if(constraintChunk.contains(nextTerminal)) {
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
					} else {
					
						//outer phyllary and middle phyllary
						if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.CONSTRAINT)) {
							Chunk chunkParent = chunkCollector.getChunk(nextTerminal);
							List<Chunk> constraintChunks = chunkParent.getChunks(ChunkType.CONSTRAINT);
							
							for(Chunk constraintChunk : constraintChunks) {
								if(constraintChunk.contains(nextTerminal)) {
									Chunk previousOrganChunk = getPreviousOrganChunk(i, terminals, chunkCollector);
									Chunk nextOrganChunk = getNextOrganChunk(i, terminals, chunkCollector);
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
		
		//connectConstraints(terminals, chunkCollector);
	}

	private void connectConstraints(List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		LinkedHashSet<Chunk> collectedChunks = new LinkedHashSet<Chunk>();
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if((terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or")) || 
					!collectedChunks.isEmpty()) {
				collectedChunks.add(terminal);
			} else {	
				Chunk chunk = chunkCollector.getChunk(terminal);
				if(chunk.isOfChunkType(ChunkType.CONSTRAINT))
					collectedChunks.addAll(chunk.getTerminals());
				else if(chunk.containsChunkType(ChunkType.ORGAN)) 
					chunkCollector.addChunk(new Chunk(ChunkType.CONSTRAINT, collectedChunks));
				else 
					collectedChunks.clear();
			}
		}
	}

	private boolean translateCharacterToConstraintForPosition(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		if(i+1<terminals.size()) {
			AbstractParseTree terminal = terminals.get(i);
			
			Chunk chunk = chunkCollector.getChunk(terminal);
			//log(LogLevel.DEBUG, chunk);
			List<Chunk> characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
			boolean hasChanged = false;
			for(Chunk characterStateChunk : characterStateChunks) {
				String character = characterStateChunk.getProperty("characterName");
				String characterState = characterStateChunk.getChunkBFS(ChunkType.STATE).getTerminalsText();
				if(character!=null && 
						((character.contains("position") || character.contains("insertion") || character.contains("structure_type") && !characterState.equals("low")) ||
								this.terminologyLearner.getTags().contains(characterState) ||
								this.terminologyLearner.getModifiers().contains(characterState))
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
			
			AbstractParseTree nextTerminal = terminals.get(i+1);
			if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.ORGAN)) {
				chunk = chunkCollector.getChunk(terminal);
				characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
				hasChanged = false;
				for(Chunk characterStateChunk : characterStateChunks) {
					Chunk stateChunk = characterStateChunk.getChunkBFS(ChunkType.STATE);
					String character = characterStateChunk.getProperty("characterName");
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
