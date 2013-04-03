package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
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

/**
 * PunctuationChunker chunks by handling punctuation terminals
 * @author rodenhausen
 */
public class PunctuationChunker extends AbstractChunker {

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
	public PunctuationChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		boolean lastWasMainSubjectOrgan = false;
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			if(chunkCollector.isOfChunkType(terminal, ChunkType.MAIN_SUBJECT_ORGAN)) {
				lastWasMainSubjectOrgan = true;
				continue;
			}
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				if(terminal.getTerminalsText().matches("\\.")) {
					chunkCollector.addChunk(new Chunk(ChunkType.END_OF_SUBCLAUSE, terminal));
				}
				if(terminal.getTerminalsText().matches("\\W")) {
					if(i==terminals.size()-1) 
						chunkCollector.addChunk(new Chunk(ChunkType.END_OF_LINE, terminal));
					else if(lastWasMainSubjectOrgan)
						chunkCollector.addChunk(new Chunk(ChunkType.END_OF_SUBCLAUSE, terminal));
					else
						chunkCollector.addChunk(new Chunk(ChunkType.COMMA, terminal));
				}
			}

			lastWasMainSubjectOrgan = false;
		}
	}
}
