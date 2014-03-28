package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

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
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
		 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, learnedCharacterKnowledgeBase);
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
