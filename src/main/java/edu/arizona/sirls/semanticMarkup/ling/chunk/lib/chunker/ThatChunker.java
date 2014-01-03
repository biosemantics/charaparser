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
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTreeFactory;
import edu.arizona.sirls.semanticMarkup.ling.pos.POS;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

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
