package semanticMarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PPINChunker extends AbstractChunker {

	@Inject
	public PPINChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		parseTree.prettyPrint();
		
		List<AbstractParseTree> ppINSubtrees = null;
		do {
			// selects the terminal
			// IN nodes and sort
			// them in the
			// original order as
			// in the text
			List<AbstractParseTree> singlePPINSubtrees = new ArrayList<AbstractParseTree>();
			Set<POS> posBs = new HashSet<POS>();
			posBs.add(POS.IN);
			posBs.add(POS.TO);
			ppINSubtrees = parseTree.getDescendants(POS.PP, posBs);
			
			for(AbstractParseTree ppINSubtree : ppINSubtrees) {
				//System.out.println("ppINSubtree " + ppINSubtree.getTerminalsText());
				// this and the next step are to
				// select PP nodes containing no
				// other PP/INs
				List<AbstractParseTree> ppINSubtreesInParent = ppINSubtree.getParent(parseTree).getDescendants(POS.PP, posBs);
				//System.out.println(ppINSubtreesInParent.size());
				if(ppINSubtreesInParent.size() == 0) {
					singlePPINSubtrees.add(ppINSubtree);
				}
			}
			extractFromSinglePPINSubtrees(parseTree, singlePPINSubtrees, chunkCollector);
		} while (ppINSubtrees.size() > 0);
	}
	
	private void extractFromSinglePPINSubtrees(IParseTree parseTree, List<AbstractParseTree> singlePPINSubtrees, ChunkCollector chunkCollector) {
		for(int i=singlePPINSubtrees.size()-1; i>=0; i--) {
			AbstractParseTree singlePPINSubtree = singlePPINSubtrees.get(i);
			
			//System.out.println("singlePPINSubtree " + singlePPINSubtree.getTerminalsText());
			AbstractParseTree in = singlePPINSubtree;
			AbstractParseTree pp = singlePPINSubtree.getParent(parseTree);
			
			if(!chunkCollector.isPartOfChunkType(in, ChunkType.PP_LIST)) {
			
				//String chaso = getOrganFrom(child);
				//both child and parent must contain an organ name to extract a relation
				//if child has no organ name, extract a constraint, location, "leaves on ...??
				//if parent has no organ name, collapse the NP. "distance of ..."
				String inTerminalText = in.getTerminalsText();
				//text of IN is not a word, e.g. "x"
				
				IParseTree firstNPTree = getFirstObjectTree(pp, in, chunkCollector);
				boolean toConnectsCharacters = isToConnectingCharacters(pp, in, chunkCollector);
				if(inTerminalText.length() < 2 || inTerminalText.matches("\\b(\\w+ly|ca|than)\\b") || firstNPTree==null || toConnectsCharacters) { 
					//dont create junk "" is no text
					collapseSubtree(parseTree, pp, POS.PP_CHECKED);
					return;
				}
				
				//System.out.println("pp " + pp);
				//System.out.println("in " + in);
				//System.out.println("firstNP " + firstNPTree);
				//System.out.println("collapse two subtrees " + in.getTerminalsText() + " "  + firstNPTree.getTerminalsText());
				IParseTree collapsedTree = this.collapseTwoSubtrees(pp, POS.COLLAPSED_PPIN, in, POS.PREPOSITION, firstNPTree, POS.OBJECT, chunkCollector);
				//parseTree.prettyPrint();
				
				chunkCollector.addChunk(this.createTwoValuedChunk(ChunkType.PP, collapsedTree, chunkCollector));
			}
		}
	}

	private boolean isToConnectingCharacters(AbstractParseTree pp,
			AbstractParseTree in, ChunkCollector chunkCollector) {
		if(in.getPOS().equals(POS.TO)) {
			List<AbstractParseTree> terminals = chunkCollector.getParseTree().getTerminals();
			int inTerminalId = terminals.indexOf(in.getTerminals().get(0));
			if(inTerminalId + 1 < terminals.size() && inTerminalId - 1 >= 0) {
				AbstractParseTree beforeInTerminal = terminals.get(inTerminalId - 1);
				AbstractParseTree inTerminal = terminals.get(inTerminalId);
				AbstractParseTree afterInTerminal =  terminals.get(inTerminalId + 1);
				Chunk beforeInChunk = chunkCollector.getChunk(beforeInTerminal); 
				Chunk inTerminalChunk = chunkCollector.getChunk(inTerminal);
				Chunk afterInChunk = chunkCollector.getChunk(afterInTerminal);
				if(beforeInChunk.equals(inTerminalChunk) && inTerminalChunk.equals(afterInChunk)) {
					//it connects to characters, but inside a single correctly formed character chunk
					return false;
				}
				
				if(beforeInChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && afterInChunk.isOfChunkType(ChunkType.CHARACTER_STATE))
					return true;
			}
		}
		return false;
	}
}
