package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * NPListChunker chunks by handling noun terminals 
 * @author rodenhausen
 */
public class NPListChunker extends AbstractChunker {

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
	public NPListChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, 
				terminologyLearner, inflector, learnedCharacterKnowledgeBase);
	}


	/**
	 * NP
	 * 	NP a
	 * 	NP b     => NP
	 * 	CC and        NNS a, b, and c 
	 * 	NP c
	 * 
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<AbstractParseTree> npCCSubTrees = parseTree.getDescendants(POS.NP, POS.CC);
		
		for(IParseTree npCCSubTree : npCCSubTrees) {
			//parseTree.prettyPrint();
			
			
			IParseTree cc = npCCSubTree;
			IParseTree np = npCCSubTree.getParent(parseTree);
			
			// all children must be either NP or NN/NNS, except for one CC, and
			// all NP child must have a child NN/NNS
			boolean isList = true;
			if (!cc.getTerminalsText().matches("(and|or|plus)")) {
				isList = false;
			}
			//List<IParseTree> ccs = np.getChildrenOfPOS(POS.CC);
			List<IParseTree> ccs = np.getDescendants(POS.CC); //Hong 3/21/14: with mostly forking ascending branches , the axis and branches or pedicels armed with firm setae : "branches , the axis and branches or pedicels" should not be a list 
			if (ccs.size() > 1) {
				isList = false;
			}
			
			List<AbstractParseTree> children = np.getChildren();
			// if(children.get(children.size()-2).getName().compareTo("CC") !=
			// 0){ //second to the last element must be CC
			// isList = false;
			// } //basal and cauline leaves=> two NN after CC
			for(IParseTree child : children) {
				POS childPOS = child.getPOS();
				if (!childPOS.equals(POS.NP) && !childPOS.equals(POS.NN) && !childPOS.equals(POS.NNS) && !childPOS.equals(POS.NNP) 
						&& !childPOS.equals(POS.CC) && !childPOS.equals(POS.PUNCT) && !childPOS.equals(POS.PRN) 
						&& !childPOS.equals(POS.ADJP) && !childPOS.equals(POS.JJ) && !childPOS.equals(POS.RB) && !childPOS.equals(POS.NONE)) { //.matches("NP|NN|NNS|CC|[^\\w]")) //"\\b(NP|NN|NNS|CC|PUNCT|PRN|ADJP|JJ|RB)\\b")){//extended on 10/29/2013, not backwards tested
					isList = false;
					break;
				}
				List<AbstractParseTree> grandChildren = child.getChildren();
				if (grandChildren.size() > 0) {
					IParseTree lastGrandChild = grandChildren.get(grandChildren.size() - 1);
					POS lastGrandChildrenPOS = lastGrandChild.getPOS();
					if (!lastGrandChild.isTerminal() && !lastGrandChildrenPOS.equals(POS.NN) && !lastGrandChildrenPOS.equals(POS.NNS) && !lastGrandChildrenPOS.equals(POS.PUNCT)) {//add PUNCT for parenthesis
						isList = false;
						break;
					}
				}
				//if(child.getDescendants(POS.ADJP).size() != 0 || child.getDescendants(POS.PP).size() != 0) {
				if(child.getDescendants(POS.PP).size() != 0) {
					isList = false;
					break;
				}
			}
			
			String collapsedPhraseText = np.getTerminalsText();

			if (collapsedPhraseText.matches(".*?\\b(" + prepositionWords + "|"
					+ units + ")\\b.*")) {
				isList = false;
			}

			if(isList) {
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : np.getTerminals()) {
					Chunk childChunk = chunkCollector.getChunk(terminal);
					childChunks.add(childChunk);
				}
				Chunk chunk = new Chunk(ChunkType.NP_LIST, childChunks); 
				chunkCollector.addChunk(chunk);
			}
			
			/*
			if (isList) {// collapse the NP
				IParseTree backup = parseTreeFactory.create();
				backup.setPOS(POS.BACKUP);
				backup.addChildren(np.getChildren());
				np.removeAllChildren();
				IParseTree collapsedNP = parseTreeFactory.create();
				collapsedNP.setPOS(POS.COLLAPSED_NP);
				IParseTree terminal = parseTreeFactory.create();
				terminal.setTerminalsText(collapsedPhraseText);
				collapsedNP.addChild(terminal);
				np.addChild(collapsedNP);
				np.addChild(backup);
			}*/
			//parseTree.prettyPrint();
		}
	}
}
