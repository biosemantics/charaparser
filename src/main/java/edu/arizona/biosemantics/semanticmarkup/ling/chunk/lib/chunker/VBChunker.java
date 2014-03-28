package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * VBChunker chunks by handling verb phrases
 * @author rodenhausen
 */
public class VBChunker extends AbstractChunker {
	
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
	 * @param posKnowledgeBase
	 */
	@Inject
	public VBChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IPOSKnowledgeBase posKnowledgeBase, 
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, learnedCharacterKnowledgeBase);
		this.posKnowledgeBase = posKnowledgeBase;
	}

	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		//parseTree.prettyPrint();
		List<AbstractParseTree> VBs = null;
		do {
			List<AbstractParseTree> filteredVBs = new ArrayList<AbstractParseTree>();
			VBs = getVBs(parseTree);
			for(AbstractParseTree vbSubTree : VBs) {
				// VP/PP should have
				// been processed in
				// PPINs
				//vbSubTree.prettyPrint();
				if(getVBs(vbSubTree).size() == 0 && vbSubTree.getChildrenOfPOS(POS.PP).size() == 0) {
					filteredVBs.add(vbSubTree);
				}
			}
			extractFromVBs(parseTree, filteredVBs, chunkCollector);
		} while (VBs.size() > 0);
		
		//parseTree.prettyPrint();
	}
	
	
	private void extractFromVBs(IParseTree parseTree, List<AbstractParseTree> vbs, ChunkCollector chunkCollector) {
		for(AbstractParseTree vb : vbs) {
			boolean sureverb = false;
			
			IParseTree grandParent = vb.getAncestor(2, parseTree);
			if(grandParent != null) {
				List<AbstractParseTree> children = grandParent.getChildren();
				if(children.get(0).getPOS().equals(POS.MD) && children.get(1).equals(vb.getParent(parseTree))) {
					sureverb = true;
				}
			}
			extractFromVB(parseTree, vb, sureverb, chunkCollector);
		}	
	}
	
	private void extractFromVB(IParseTree parseTree, AbstractParseTree vb, boolean sureverb, ChunkCollector chunkCollector) {
		AbstractParseTree vp = vb.getParent(parseTree);
		if(vp == null){
			return; //lVB is root
		}
		
		String theVerb = vb.getTerminalsText();
		IParseTree firstNPTree = this.getFirstObjectTree(vp, vb, chunkCollector);
		if(!sureverb && (theVerb.length() < 2 || theVerb.matches("\\b(\\w+ly|ca)\\b") 
		   || posKnowledgeBase.getMostLikleyPOS(theVerb) == null || 
		   !posKnowledgeBase.getMostLikleyPOS(theVerb).equals(POS.VB)) || firstNPTree==null) { //text of V is not a word, e.g. "x"
			//dont create junk "" is no text
			collapseSubtree(parseTree, vp, POS.VP_CHECKED);
			return;
		}
		//do extraction here
		//print(VP, child, "", chaso);

		AbstractParseTree collapseRoot = this.collapseTwoSubtrees(vp, POS.COLLAPSED_VB, vb, POS.VERB, firstNPTree, POS.OBJECT, chunkCollector);
		createTwoValuedChunk(ChunkType.VP, collapseRoot, chunkCollector);
	}
	
	private List<AbstractParseTree> getVBs(IParseTree parseTree) {
		List<AbstractParseTree> VBs = parseTree.getDescendants(POS.VP, POS.VBD);
		VBs.addAll(parseTree.getDescendants(POS.VP, POS.VBG));
		VBs.addAll(parseTree.getDescendants(POS.VP, POS.VBN));
		VBs.addAll(parseTree.getDescendants(POS.VP, POS.VBP));
		VBs.addAll(parseTree.getDescendants(POS.VP, POS.VBZ));
		VBs.addAll(parseTree.getDescendants(POS.VP, POS.VB));
		return VBs;
	}
}
