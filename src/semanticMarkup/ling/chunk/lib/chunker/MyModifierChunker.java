package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.know.IPOSKnowledgeBase;
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
 * MyModifierChunker chunks by handling modifier terminals
 * @author rodenhausen
 */
public class MyModifierChunker extends AbstractChunker {

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
	 * @param organStateKnowledgeBase
	 * @param posKnowledgeBase
	 */
	@Inject
	public MyModifierChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase, 
			@Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
		this.posKnowledgeBase = posKnowledgeBase;
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  {
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal) && posKnowledgeBase.isAdverb(terminal.getTerminalsText())) {
				chunkCollector.addChunk(new Chunk(ChunkType.MODIFIER, terminal));
			}
			
		}
	}
}
