package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.name.Named;

/**
 * UnitsChunker chunks by handling "unit" terminals
 * @author rodenhausen
 */
public class UnitsChunker extends AbstractChunker {

	public UnitsChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
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
