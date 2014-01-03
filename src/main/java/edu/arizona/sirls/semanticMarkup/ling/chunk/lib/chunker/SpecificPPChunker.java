package edu.arizona.sirls.semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.AbstractChunker;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkCollector;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;
import edu.arizona.sirls.semanticMarkup.ling.parse.AbstractParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTreeFactory;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

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
	 * @param organStateKnowledgeBase
	 */
	@Inject
	public SpecificPPChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
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
