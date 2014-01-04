package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * CharacterListChunker chunks by handling a number of character describing terminals.
 * 
 * TODO: This has to be the first chunker, as the tree will be rearranged.
 * Hence, chunkCollector will loose the references in the hashmap of terminal Id -> chunk if tree is normalized at a later step
 * It would be an option to let chunkCollector be aware of changes in the tree, or internally use a hashmap of AbstractParseTree (terminal) -> chunk
 * However, hashing in AbstractParseTree is implemented such that two same named terminals get the same hash. Hash function would have to be rewritten.
 * 
 * @author rodenhausen
 */
public class CharacterListChunker extends AbstractChunker {

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
	 * @param posKnowledgeBase
	 */
	@Inject
	public CharacterListChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			@Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector, organStateKnowledgeBase);
		this.posKnowledgeBase = posKnowledgeBase;
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  { 
			String terminalsText = terminal.getTerminalsText();
			

			if(terminalsText.contains("~list~")) {
				normalizeCharacterList(terminal, chunkCollector);
			}
			else if(terminalsText.equals("moreorless")) {
				normalizeMoreOrLess(terminal, chunkCollector);
			}
			else if(terminalsText.contains("~")) {
				normalizeModifiers(terminal, chunkCollector);
			} 
			else if(terminalsText.contains("_c_")) {
				normalizeConnectedColors(terminal, chunkCollector);
			}
		}
		//System.out.println(chunkCollector.getParseTree().prettyPrint());
	}
	
	private void normalizeConnectedColors(AbstractParseTree terminal,
			ChunkCollector chunkCollector) {
		terminal.setTerminalsText(terminal.getTerminalsText().replaceAll("_c_", " "));
		Chunk colorStateChunk = new Chunk(ChunkType.STATE, terminal);
		Chunk colorChunk = new Chunk(ChunkType.CHARACTER_STATE, colorStateChunk);
		colorChunk.setProperty("characterName", "coloration");
		chunkCollector.addChunk(colorChunk);
		return; 
	}

	private void normalizeModifiers(AbstractParseTree terminal,
			ChunkCollector chunkCollector) {
		String[] modifierTokens = terminal.getTerminalsText().split("~");
		terminal.setPOS(POS.ADVP);
		for(String modifierToken : modifierTokens) {
			//disconnect connected colors
			modifierToken = modifierToken.replaceAll("_c_", " ");
			if(posKnowledgeBase.isAdverb(modifierToken)) {
				AbstractParseTree modifierTerminal = parseTreeFactory.create();
				modifierTerminal.setTerminalsText(modifierToken);
				terminal.addChild(modifierTerminal);
				Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifierTerminal);
				chunkCollector.addChunk(modifierChunk);
			} else {
				if((modifierToken.equals(",") || modifierToken.equals("and") || modifierToken.equals("or") || modifierToken.equals("to"))) {
					if(modifierToken.equals(",")) {
						AbstractParseTree punctTree = parseTreeFactory.create();
						punctTree.setPOS(POS.PUNCT);
						AbstractParseTree punct = parseTreeFactory.create();
						punct.setTerminalsText(modifierToken);
						punctTree.addChild(punct);
						terminal.addChild(punctTree);
					} else if(modifierToken.equals("and")) {
						AbstractParseTree ccTree = parseTreeFactory.create();
						ccTree.setPOS(POS.CC);
						AbstractParseTree cc = parseTreeFactory.create();
						cc.setTerminalsText(modifierToken);
						ccTree.addChild(cc);
						terminal.addChild(ccTree);
					} else if(modifierToken.equals("or")) {
						AbstractParseTree ccTree = parseTreeFactory.create();
						ccTree.setPOS(POS.CC);
						AbstractParseTree cc = parseTreeFactory.create();
						cc.setTerminalsText(modifierToken);
						ccTree.addChild(cc);
						terminal.addChild(ccTree);
					} else if(modifierToken.equals("to")) {
						AbstractParseTree toTree = parseTreeFactory.create();
						toTree.setPOS(POS.TO);
						AbstractParseTree to = parseTreeFactory.create();
						to.setTerminalsText(modifierToken);
						toTree.addChild(to);
						terminal.addChild(toTree);
					} 
				}
			}	
		}
	}

	private void normalizeMoreOrLess(AbstractParseTree terminal,
			ChunkCollector chunkCollector) {
		AbstractParseTree parseTree = chunkCollector.getParseTree();
		AbstractParseTree parent = terminal.getParent(parseTree);
		AbstractParseTree moreTree = terminal;
		moreTree.setTerminalsText("more");
		AbstractParseTree orTree = this.parseTreeFactory.create();
		orTree.setTerminalsText("or");
		AbstractParseTree lessTree = this.parseTreeFactory.create();
		lessTree.setTerminalsText("less");
		parent.addChild(orTree);
		parent.addChild(lessTree);
		//create tree structure 
		//add them all as one modifier chunk
		//terminal.setTerminalsText("more or less");
		LinkedHashSet<Chunk> modifierChunks = new LinkedHashSet<Chunk>();
		modifierChunks.add(moreTree);
		modifierChunks.add(orTree);
		modifierChunks.add(lessTree);
		Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifierChunks);
		chunkCollector.addChunk(modifierChunk);
		return; 
	}

	private void normalizeCharacterList(AbstractParseTree terminal, ChunkCollector chunkCollector) {
		String terminalsText = terminal.getTerminalsText();
		
		//terminalsText = terminalsText.replaceAll("ttt", "");
		String[] characterListParts = terminalsText.split("~list~");
		String character = characterListParts[0];
		if(character.equals("colorationttt"))
			character = character.replaceAll("ttt", "");
		String stateList = characterListParts[1];
		stateList = stateList.replaceAll("~", " ");
		stateList = stateList.replaceAll(" punct ", " , ");
		
		String[] modifierStateTokens = stateList.split("\\s");
		List<String> modifierStateTokensList = Arrays.asList(modifierStateTokens);
		//List<Chunk> modifierChunks = new LinkedList<Chunk>();
		
		terminal.setPOS(POS.ADJP);
		
		
		AbstractParseTree modifiersTree = null;
		AbstractParseTree statesTree = null;
		boolean newState = true;
		LinkedHashSet<Chunk> characterStateChildChunks = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> stateChildChunks = new LinkedHashSet<Chunk>();
		
		//for two following character chunks, correct first one to be modifier
		//Chunk previousCharacter = null;
		//AbstractParseTree previousStateTree = null;
		Chunk characterChunk = null;
		LinkedHashSet<Chunk> toChunks = new LinkedHashSet<Chunk>();
		boolean collectToChunks = false;
		for(String modifierStateToken : modifierStateTokensList) {
			//disconnect connected color strings
			modifierStateToken = modifierStateToken.replaceAll("_c_", " ");
			
			if(newState) {
				if(!stateChildChunks.isEmpty()) {
					Chunk stateChunk = new Chunk(ChunkType.STATE, stateChildChunks);
					characterStateChildChunks.add(stateChunk);
					characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildChunks);
					characterChunk.setProperty("characterName", character);
					chunkCollector.addChunk(characterChunk);
					//previousCharacter = characterChunk;
					//previousStateTree = statesTree;
					stateChildChunks.clear();
					characterStateChildChunks.clear();
					statesTree = null;
					modifiersTree = null;
					
					if(collectToChunks) {
						toChunks.add(characterChunk);
						chunkCollector.addChunk(new Chunk(ChunkType.TO_PHRASE, toChunks));
						toChunks.clear();
						collectToChunks = false;
					}
				}
			}
			
			if(modifierStateToken.equals("to")) {
				toChunks.add(characterChunk);
				AbstractParseTree toTree = parseTreeFactory.create();
				toTree.setPOS(POS.TO);
				terminal.addChild(toTree);
				AbstractParseTree to = parseTreeFactory.create();
				to.setTerminalsText(modifierStateToken);
				toTree.addChild(to);
				toChunks.add(new Chunk(ChunkType.TO, to));
				collectToChunks = true;
				newState = false;
			} else if(posKnowledgeBase.isAdverb(modifierStateToken)) {
				if(modifiersTree == null) {
					modifiersTree = parseTreeFactory.create();
					modifiersTree.setPOS(POS.ADVP);
					terminal.addChild(modifiersTree);
					newState = false;
				}
				/*if(newState) {
					if(!stateChildChunks.isEmpty()) {
						Chunk stateChunk = new Chunk(ChunkType.STATE, stateChildChunks);
						characterStateChildChunks.add(stateChunk);
						Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildChunks);
						characterChunk.setProperty("characterName", character);
						chunkCollector.addChunk(characterChunk);
						stateChildChunks.clear();
						characterStateChildChunks.clear();
					}
					modifiersTree = parseTreeFactory.create();
					modifiersTree.setPOS(POS.ADVP);
					terminal.addChild(modifiersTree);
					
					statesTree = null;
					newState = false;
				}*/
				AbstractParseTree modifierTerminal = parseTreeFactory.create();
				if(modifierStateToken.equals("moreorless"))
					modifierStateToken = "more or less";
				modifierTerminal.setTerminalsText(modifierStateToken);
				modifiersTree.addChild(modifierTerminal);
				Chunk characterStateChildChunk = new Chunk(ChunkType.MODIFIER, modifierTerminal);
				characterStateChildChunks.add(characterStateChildChunk);
				//previousCharacter = null;
				//previousStateTree = null;
			} else { //!modifierStateToken.equals(",") && !modifierStateToken.equals("and") && !modifierStateToken.equals("or")posKnowledgeBase.isAdjective(modifierStateToken)
				if(newState && (modifierStateToken.equals(",") || modifierStateToken.equals("and") || modifierStateToken.equals("or"))) {
					if(newState && modifierStateToken.equals(",")) {
						AbstractParseTree punctTree = parseTreeFactory.create();
						punctTree.setPOS(POS.PUNCT);
						AbstractParseTree punct = parseTreeFactory.create();
						punct.setTerminalsText(modifierStateToken);
						punctTree.addChild(punct);
						terminal.addChild(punctTree);
						newState = true;
					} else if(newState && modifierStateToken.equals("and")) {
						AbstractParseTree ccTree = parseTreeFactory.create();
						ccTree.setPOS(POS.CC);
						AbstractParseTree cc = parseTreeFactory.create();
						cc.setTerminalsText(modifierStateToken);
						ccTree.addChild(cc);
						terminal.addChild(ccTree);
						newState = true;
					} else if(modifierStateToken.equals("or")) {
						AbstractParseTree ccTree = parseTreeFactory.create();
						ccTree.setPOS(POS.CC);
						AbstractParseTree cc = parseTreeFactory.create();
						cc.setTerminalsText(modifierStateToken);
						ccTree.addChild(cc);
						terminal.addChild(ccTree);
						newState = true;
					} 
					//previousCharacter = null;
					//previousStateTree = null;
				} else {						
					if(statesTree == null) {
						statesTree = parseTreeFactory.create();
						statesTree.setPOS(POS.ADJP);
						terminal.addChild(statesTree);
					}
					
					//immediately preceeding token was processed as state too, make it a modifier instead
					//states are correctly separated by punctuation or and/or
					/*if(previousCharacter != null && previousStateTree != null) {
						LinkedHashSet<Chunk> modifierChunks = new LinkedHashSet<Chunk>();
						modifierChunks.addAll(previousCharacter.getTerminals());
						Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifierChunks);
						characterStateChildChunks.add(modifierChunk);
						previousStateTree.setPOS(POS.ADVP);
					}*/
					
					AbstractParseTree stateTerminal = parseTreeFactory.create();
					stateTerminal.setTerminalsText(modifierStateToken);
					statesTree.addChild(stateTerminal);
					Chunk stateChildChunk = stateTerminal;
					stateChildChunks.add(stateChildChunk);
					newState = true;
				}
			}
		}
		//last one
		Chunk stateChunk = new Chunk(ChunkType.STATE, stateChildChunks);
		characterStateChildChunks.add(stateChunk);
		characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildChunks);
		characterChunk.setProperty("characterName", character);
		chunkCollector.addChunk(characterChunk);
		
		if(collectToChunks) {
			toChunks.add(characterChunk);
			chunkCollector.addChunk(new Chunk(ChunkType.TO_PHRASE, toChunks));
			toChunks.clear();
			collectToChunks = false;
		}
	}
}
