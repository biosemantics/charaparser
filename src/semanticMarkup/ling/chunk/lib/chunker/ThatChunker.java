package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
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
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ThatChunker chunks by handling "that" terminals
 * @author rodenhausen
 */
public class ThatChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 */
	@Inject
	public ThatChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
	}

	/**
	 * (SBAR
              (WHNP (WDT that))
              (S
                (VP (VBP resemble)
                  (NP (NN tacks)))))
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<IParseTree> thatSubTrees = parseTree.getDescendants(POS.SBAR, POS.WHNP, "that");
		thatSubTrees.addAll(parseTree.getDescendants(POS.SBAR, POS.WHNP, "which"));

		for(IParseTree thatSubTree : thatSubTrees) {
			log(LogLevel.DEBUG, thatSubTrees.toString());
			IParseTree whnp = thatSubTree.getParent(parseTree);
			IParseTree sbar = whnp.getParent(parseTree);
			//collapseSubtree(parseTree, sbar, sbar.getTerminalsText(), POS.COLLAPSED_THAT);

			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : sbar.getTerminals()) {
				Chunk childChunk = chunkCollector.getChunk(terminal);
				if(!childChunks.contains(childChunk))
					childChunks.add(childChunk);
			}
			Chunk thatChunk = new Chunk(ChunkType.THAT, childChunks);
			chunkCollector.addChunk(thatChunk);
		}
		
		//parseTree.prettyPrint();
	}
}
