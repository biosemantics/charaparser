package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.pos.POS;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * PPINChunker chunks PP followed by IN tags of the parse tree
 * @author rodenhausen
 */
public class PPINChunker extends AbstractChunker {

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
	public PPINChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector,  learnedCharacterKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		//parseTree.prettyPrint();
		
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
				//log(LogLevel.DEBUG, "ppINSubtree " + ppINSubtree.getTerminalsText());
				// this and the next step are to
				// select PP nodes containing no
				// other PP/INs
				/* strange behavior to be checked 5/21/2015, the for-statement changed chunks in chunckCollector silently
				 * if(chunkCollector.getChunk(ppINSubtree.getTerminals().get(0)).isOfChunkType(ChunkType.CHARACTER_STATE) && 
						chunkCollector.getChunk(ppINSubtree).getProperty("character_name").compareTo("character")==0) //length of 
				{
					log(LogLevel.DEBUG, "PPINChunker, did it");
					continue;
				}*/
				List<AbstractParseTree> ppINSubtreesInParent = ppINSubtree.getParent(parseTree).getDescendants(POS.PP, posBs);
				//log(LogLevel.DEBUG, ppINSubtreesInParent.size());
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
			
			//log(LogLevel.DEBUG, "singlePPINSubtree " + singlePPINSubtree.getTerminalsText());
			AbstractParseTree in = singlePPINSubtree;
			AbstractParseTree pp = singlePPINSubtree.getParent(parseTree);
			
			if(!chunkCollector.isPartOfChunkType(in, ChunkType.PP_LIST)) {
			
				//String chaso = getOrganFrom(child);
				//both child and parent must contain an organ name to extract a relation
				//if child has no organ name, extract a constraint, location, "leaves on ...??
				//if parent has no organ name, collapse the NP. "distance of ..."
				String inTerminalText = in.getTerminalsText();
				
				
				IParseTree firstNPTree = getFirstObjectTree(pp, in, chunkCollector);
				
				boolean toConnectsCharacters = isToConnectingCharacters(pp, in, chunkCollector);//"pink to red"
				//text of IN is not a word, e.g. "x"
				if(chunkCollector.isPartOfChunkType(in.getTerminals().get(0), ChunkType.COMPARATIVE_VALUE)||inTerminalText.length() < 2 || inTerminalText.matches("\\b(\\w+ly|ca|than)\\b") || firstNPTree==null || toConnectsCharacters)//COMPARATIVE_VALUE may constains PPs such as of, than, as-long-as, but they don't belong to PPChunks
				{ 
					//dont create junk "" is no text
					collapseSubtree(parseTree, pp, POS.PP_CHECKED);
					return;
				}
				//TODO Hong: nested pp-chunks? Handled. 
				//log(LogLevel.DEBUG, "pp " + pp);
				//log(LogLevel.DEBUG, "in " + in);
				//log(LogLevel.DEBUG, "firstNP " + firstNPTree);
				//log(LogLevel.DEBUG, "collapse two subtrees " + in.getTerminalsText() + " "  + firstNPTree.getTerminalsText());
				AbstractParseTree collapsedTree = this.collapseTwoSubtrees(pp, POS.COLLAPSED_PPIN, in, POS.PREPOSITION, firstNPTree, POS.OBJECT, chunkCollector);
				//parseTree.prettyPrint();
				
				
				createTwoValuedChunk(ChunkType.PP, collapsedTree, chunkCollector);
				//deal with a special case: length of leaves, adaxial of leaves
				charaPP(chunkCollector, singlePPINSubtree);
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
