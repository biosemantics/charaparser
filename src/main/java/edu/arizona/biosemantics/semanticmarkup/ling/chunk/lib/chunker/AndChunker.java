package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * AndChunker chunks by handling "and" terminals
 * @author rodenhausen
 */
public class AndChunker extends AbstractChunker {

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
	public AndChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,  ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, learnedCharacterKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		Chunk previousChunk = null;
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i<terminals.size(); i++)  {
			AbstractParseTree terminal = terminals.get(i);
			//Hong 3/23/2014 doesn't make sense to merge: petals mostly imbricated [in the [bud and usually inserted with the stamens]]
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal) && terminal.getTerminalsText().equals("and")) {
				chunkCollector.addChunk(new Chunk(ChunkType.AND, terminal));
				/*if(previousChunk != null && previousChunk.isOfChunkType(ChunkType.PP)) {
					connectOrganWithPP(i, previousChunk, terminals, chunkCollector);
				}*/
			}
			if(chunkCollector.isPartOfANonTerminalChunk(terminal) && terminal.getTerminalsText().equals("and")) {
				Chunk chunk = chunkCollector.getChunk(terminal);
				chunk.replaceChunk(terminal, new Chunk(ChunkType.AND, terminal));
				chunkCollector.addChunk(chunk);
			}
			previousChunk = chunkCollector.getChunk(terminal);
		}
	}
	
	private void connectOrganWithPP(int i,  Chunk ppChunk, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		boolean foundOrgan = false;
		LinkedHashSet<Chunk> collectedChunks = new LinkedHashSet<Chunk>();
		for(; i<terminals.size(); i++) {
			AbstractParseTree lookAheadTerminal = terminals.get(i);
			if(!chunkCollector.isPartOfANonTerminalChunk(lookAheadTerminal)) {
				collectedChunks.add(lookAheadTerminal);
			} else {
				Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);
				/*if(lookAheadChunk.isOfChunkType(ChunkType.COMMA)) {
					collectedChunks.add(lookAheadChunk);
				}
				if(lookAheadChunk.isOfChunkType(ChunkType.AND)) {
					collectedChunks.add(lookAheadChunk);
				}
				if(lookAheadChunk.isOfChunkType(ChunkType.OR)) {
					collectedChunks.add(lookAheadChunk);
				}
				if(lookAheadChunk.isOfChunkType(ChunkType.CHARACTER_STATE)) {
					collectedChunks.add(lookAheadChunk);
				}
				if(lookAheadChunk.isOfChunkType(ChunkType.STATE)) {
					collectedChunks.add(lookAheadChunk);
				}
				if(lookAheadChunk.isOfChunkType(ChunkType.CONSTRAINT)) {
					collectedChunks.add(lookAheadChunk);
				}*/
				collectedChunks.add(lookAheadChunk);
				if(lookAheadChunk.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) {
					//collectedChunks.add(lookAheadChunk);
					foundOrgan = true;
					break;
				}
			}
		}
		if(foundOrgan) {
			Chunk objectChunk = ppChunk.getChunkBFS(ChunkType.OBJECT);
			objectChunk.getChunks().addAll(collectedChunks);
			chunkCollector.addChunk(ppChunk);
		}
	}
}
