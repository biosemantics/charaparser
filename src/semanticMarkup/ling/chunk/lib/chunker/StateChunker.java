package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
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

public class StateChunker extends AbstractChunker {

	private IOrganStateKnowledgeBase organStateKnowledgeBase;

	@Inject
	public StateChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
		this.organStateKnowledgeBase = organStateKnowledgeBase;
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
