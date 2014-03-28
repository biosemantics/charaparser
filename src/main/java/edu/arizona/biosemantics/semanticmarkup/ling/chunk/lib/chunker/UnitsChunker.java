package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;



import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * UnitsChunker chunks by handling "unit" terminals
 * @author rodenhausen
 */
public class UnitsChunker extends AbstractChunker {

	public UnitsChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector,learnedCharacterKnowledgeBase);
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
