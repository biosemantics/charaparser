package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * Specific PP Chunker chunks preposition phrases preceeded by a state
 * @author rodenhausen/
 */
public class SpecificPPChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param learnedCharacterKnowledgeBase
	 */
	@Inject
	public SpecificPPChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters,
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector, learnedCharacterKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i<terminals.size(); i++)  {
			AbstractParseTree terminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(terminal);
			if(terminalChunk.isOfChunkType(ChunkType.CHARACTER_STATE) || terminalChunk.isOfChunkType(ChunkType.STATE)) {
				LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
				function.add(terminalChunk);
				Chunk functionChunk = new Chunk(ChunkType.SPECIFIER, function);

				LinkedHashSet<Chunk> specificPP = new LinkedHashSet<Chunk>();
				specificPP.add(functionChunk);

				i+=terminalChunk.size();

				for(;i<terminals.size(); i++) {
					AbstractParseTree lookAheadTerminal = terminals.get(i);
					Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);

					/*if(lookAheadChunk.isOfChunkType(ChunkType.MODIFIER)) {
						specificPP.add(lookAheadChunk);
					} else*/ if(lookAheadChunk.isOfChunkType(ChunkType.PP)) {
						specificPP.add(lookAheadChunk);
						Chunk specificPPChunk = new Chunk(ChunkType.SPECIFIC_PP, specificPP);
						chunkCollector.addChunk(specificPPChunk);
						break;
					} else {
						break;
					}
				}
			}
		}
	}

}
