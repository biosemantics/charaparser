package semanticMarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * MyStateChunker chunks by handling state terminals 
 * @author rodenhausen
 */
public class MyStateChunker extends AbstractChunker {

	private ICharacterKnowledgeBase characterKnowledgeBase;
	private String or = "_or_";
	private IPOSKnowledgeBase posKnowledgeBase;

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
	 * @param characterKnowledgeBase
	 * @param posKnowledgeBase
	 */
	@Inject
	public MyStateChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase, 
			ICharacterKnowledgeBase characterKnowledgeBase, IPOSKnowledgeBase posKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
		this.organStateKnowledgeBase = organStateKnowledgeBase;
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.posKnowledgeBase = posKnowledgeBase;
	}

	private boolean[] getStateNotModifier(List<AbstractParseTree> terminals) {
		boolean[] result = new boolean[terminals.size()];
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			
			boolean validCharacterState = organStateKnowledgeBase.isState(terminal.getTerminalsText()) && characterKnowledgeBase.containsCharacterState(terminal.getTerminalsText());
			boolean adverbCharacterState = posKnowledgeBase.isAdverb(terminal.getTerminalsText()) && i+1 < terminals.size() &&
					organStateKnowledgeBase.isState(terminals.get(i+1).getTerminalsText()) && characterKnowledgeBase.containsCharacterState(terminal.getTerminalsText());
			boolean stateNotModifier = validCharacterState && !adverbCharacterState;
			result[i] = stateNotModifier;
		}
		return result;
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<Chunk> modifierChunks = new ArrayList<Chunk>();
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		
		boolean stateNotModifier[] = getStateNotModifier(terminals);
		
		for(int i=0; i<terminals.size(); i++)  { 			
			AbstractParseTree terminal = terminals.get(i);
			Chunk chunk = chunkCollector.getChunk(terminal);
			
			if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE) || 
					chunk.isOfChunkType(ChunkType.TO_PHRASE) || 
					chunk.isOfChunkType(ChunkType.CONSTRAINT)) 
				continue;
			
			if(chunk.isOfChunkType(ChunkType.MODIFIER) && !stateNotModifier[i]) {
				modifierChunks.add(chunk);
				continue;
			}
			
			if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && terminal.getTerminalsText().equals("no")) {
				Chunk stateChunk = new Chunk(ChunkType.STATE, terminal);
				chunkCollector.addChunk(stateChunk);
				Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, stateChunk);
				characterChunk.setProperty("characterName", "presence");
				chunkCollector.addChunk(characterChunk);
			}
			
			if(organStateKnowledgeBase.isState(terminal.getTerminalsText())) {
				String character = null;
				if(characterKnowledgeBase.containsCharacterState(terminal.getTerminalsText())) {
					character = characterKnowledgeBase.getCharacterName(terminal.getTerminalsText());
				}
				
				Chunk stateChunk = new Chunk(ChunkType.STATE, terminal);
				chunkCollector.addChunk(stateChunk);
				List<Chunk> characterStateChildChunks = new ArrayList<Chunk>();
				characterStateChildChunks.addAll(modifierChunks);
				characterStateChildChunks.add(stateChunk);
				Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildChunks);
				
				if(character != null) {
					characterStateChunk.setProperty("characterName", character);
					chunkCollector.addChunk(characterStateChunk);
				} 
				modifierChunks.clear();
			} else {
				modifierChunks.clear();
			}
		}
		
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			
			if(i-1>=0 && i+1 < terminals.size()) {
				
				//TODO use ChunkType.TO
				if(terminal.getTerminalsText().equals("to")) {
					AbstractParseTree nextTerminal = terminals.get(i+1);
					AbstractParseTree previousTerminal = terminals.get(i-1);

					Chunk nextChunk = chunkCollector.getChunk(nextTerminal);
					Chunk previousChunk = chunkCollector.getChunk(previousTerminal);
					
					if(!nextChunk.equals(previousChunk)) {
						if(nextChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && 
								previousChunk.isOfChunkType(ChunkType.CHARACTER_STATE)) {
							Set<String> characterOverlap = characterNamesOverlap(nextChunk, previousChunk);
							
							if(!characterOverlap.isEmpty()) {
								fixParseTreeForToCharacter(i, terminals, chunkCollector.getParseTree());	
								//chunkCollector.getParseTree().prettyPrint();
								
								String characterString = "";
								Iterator<String> characterIterator = characterOverlap.iterator();
								while(characterIterator.hasNext()) {
									characterString += characterIterator.next();
									if(characterIterator.hasNext())
										characterString += or;
								}
								
								Chunk stateChunk = new Chunk(ChunkType.STATE);
								LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
								chunks.add(previousTerminal);
								chunks.add(terminal);
								chunks.add(nextTerminal);
								stateChunk.setChunks(chunks);
								
								Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, stateChunk);
								characterStateChunk.setProperty("characterName", characterString);
								
								chunkCollector.addChunk(characterStateChunk);
							}
						}
					}
				}	
			}
		}
	}

	private void fixParseTreeForToCharacter(int i, List<AbstractParseTree> terminals, IParseTree parseTree) {
		//parseTree.prettyPrint();
		AbstractParseTree previousTerminal = terminals.get(i-1);
		AbstractParseTree terminal = terminals.get(i);
	
		if(parseTree.getDepth(previousTerminal) != parseTree.getDepth(terminal)) {
			//from previous iteration 
			IParseTree beforePreviousParent = previousTerminal;
			IParseTree beforeTerminalParent = terminal;
			
			//IParseTree lastPreviousParent = previousTerminal;
			
			//this iteration
			IParseTree terminalParent = terminal;
			IParseTree previousParent = previousTerminal;

			int previousAncestorHeight = 1;
			int terminalAncestorHeight = 1;
			while(true) {
				
				boolean previousParentHasChanged = false;
				boolean terminalParentHasChanged = false;
				if(parseTree.getDepth(previousParent) < parseTree.getDepth(terminalParent)) {
					terminalParent = terminal.getAncestor(terminalAncestorHeight, parseTree);
					terminalParentHasChanged = true;
				} else if(parseTree.getDepth(previousParent) > parseTree.getDepth(terminalParent)) {
					previousParent = previousTerminal.getAncestor(previousAncestorHeight, parseTree);
					previousParentHasChanged = true;
				} else {
					terminalParent = terminal.getAncestor(terminalAncestorHeight, parseTree);
					previousParent = previousTerminal.getAncestor(previousAncestorHeight, parseTree);
					previousParentHasChanged = true;
					terminalParentHasChanged = true;
				}
	
				if(previousParent.equals(terminalParent)) {
					/*
					while(true) {
						lastPreviousParent = previousTerminal.getAncestor(--j, parseTree);
						if(!lastPreviousParent.equals(previousParent))
							break;
					}*/
					int beforePreviousParentIndex = terminalParent.indexOf(beforePreviousParent);
					int beforeTerminalParentIndex = terminalParent.indexOf(beforeTerminalParent); //beforePreviousParentIndex + beforePreviousParent.getChildren().size();
					terminalParent.removeChild(beforePreviousParent);
					terminalParent.addChildren(beforePreviousParentIndex, beforePreviousParent.getChildren());
					terminalParent.removeChild(beforeTerminalParent);
					terminalParent.addChildren(beforeTerminalParentIndex, beforeTerminalParent.getChildren());
					break;
				}
				
				//if(previousParentHasChanged) {
				//	lastPreviousParent = previousParent;
				//}
				if(previousParentHasChanged) {
					beforePreviousParent = previousParent;
					previousAncestorHeight++;
				}
				if(terminalParentHasChanged) {
					beforeTerminalParent = terminalParent;
					terminalAncestorHeight++;
				}
			}		
		}
	}

	private Set<String> characterNamesOverlap(Chunk nextChunk, Chunk previousChunk) {
		String nextCharacterName = nextChunk.getProperty("characterName");
		String previousCharacterName = previousChunk.getProperty("characterName");
		String[] nextCharacterNames = nextCharacterName.split(or);
		String[] previousCharacterNames = previousCharacterName.split(or);
		Set<String> nextCharacterSet = getCharacterSet(nextCharacterNames);
		Set<String> previousCharacterSet = getCharacterSet(previousCharacterNames);
		nextCharacterSet.retainAll(previousCharacterSet);
		return nextCharacterSet;
	}

	private Set<String> getCharacterSet(String[] characterNames) {
		Set<String> characters = new HashSet<String>();
		for(String characterName : characterNames) {
			characters.add(characterName);
		}
		return characters;
	}


}
