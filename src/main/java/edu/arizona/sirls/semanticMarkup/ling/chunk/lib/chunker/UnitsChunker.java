package edu.arizona.sirls.semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.AbstractChunker;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkCollector;
import edu.arizona.sirls.semanticMarkup.ling.parse.AbstractParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTreeFactory;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

/**
 * UnitsChunker chunks by handling "unit" terminals
 * @author rodenhausen
 */
public class UnitsChunker extends AbstractChunker {

	public UnitsChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
	}

	/**
	 * 3] {mm}
	 * 
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree parseTree = terminals.get(i);
			if(parseTree.getTerminalsText().matches(this.units)) {
				if(i-1 >= 0) {
					AbstractParseTree lookBehindTerminal = terminals.get(i-1);
					if(chunkCollector.isPartOfANonTerminalChunk(lookBehindTerminal) && 
							lookBehindTerminal.getTerminalsText().matches(".*?\\d")) {		
						Chunk chunk = chunkCollector.getChunk(lookBehindTerminal);
						LinkedHashSet<Chunk> childChunks = chunk.getChunks();
						childChunks.add(chunkCollector.getChunk(parseTree));
						chunk = new Chunk(chunk.getChunkType(), childChunks);
						chunkCollector.addChunk(chunk);
					}
				}
			}
		}
	}
}
