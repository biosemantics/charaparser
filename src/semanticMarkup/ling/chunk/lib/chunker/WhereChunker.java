package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * WhenChunker chunks by handling 'where' terminals
 * @author rodenhausen
 */
public class WhereChunker extends AbstractChunker {

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
	public WhereChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
	}

	/**
	 *     (SBAR
        (WHADVP (WRB where))
        (S
          (NP (NN spine) (NNS bases))
          (ADJP (JJ tend)
            (S
              (VP (TO to)
                (VP (VB be)
                  (ADJP (JJ elongate))))))))       
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		
		for(AbstractParseTree terminal : parseTree.getTerminals()) {
			if(terminal.getTerminalsText().equals("where") && !chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				Chunk whereChunk = new Chunk(ChunkType.WHERE, terminal);
				chunkCollector.addChunk(whereChunk);
			}
		}
		
		/*IParseTree parseTree = chunkCollector.getParseTree();
		List<IParseTree> thatSubTrees = parseTree.getDescendants(POS.SBAR, POS.WHADVP, "where");
		
		for(IParseTree whereSubTree : thatSubTrees) {
			IParseTree whnp = whereSubTree.getParent(parseTree);
			IParseTree sbar = whnp.getParent(parseTree);
			//collapseSubtree(parseTree, sbar, sbar.getTerminalsText(), POS.COLLAPSED_WHERE);
		
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : sbar.getTerminals()) {
				Chunk childChunk = chunkCollector.getChunk(terminal);
				if(!childChunks.contains(childChunk))
					childChunks.add(childChunk);
			}
			Chunk whereChunk = new Chunk(ChunkType.WHERE, childChunks);
			chunkCollector.addChunk(whereChunk);
		}*/
	}
}
