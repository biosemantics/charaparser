package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * StateChunker chunks by handling state terminals
 * @author rodenhausen
 */
public class StateChunker extends AbstractChunker {

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
	public StateChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  { 
			//collapse here? no decided to create chunk on the fly 
			//terminalParent = terminal.getParent(parseTree);
			Chunk chunk = chunkCollector.getChunk(terminal);
			if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && organStateKnowledgeBase.isState(terminal.getTerminalsText())) {
				chunkCollector.addChunk(new Chunk(ChunkType.STATE, terminal));
			}
		}
	}
}
