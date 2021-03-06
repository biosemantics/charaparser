package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.pos.POS;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * PPListChunker chunks conjuncted preposition phrases
 * PP: [PREPOSITION: [PP_LIST: [at, chelicerae, bases, and, at]], OBJECT: [leg, iii]]
 * @author rodenhausen
 */
public class PPListChunker extends AbstractChunker {

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
	public PPListChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters,
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary,
				terminologyLearner, inflector, learnedCharacterKnowledgeBase);
	}

	/** PP
	 * 	IN on
	 *  CC or    to PP
	 * 	IN above      IN on or above
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<AbstractParseTree> ppCCSubTrees = parseTree.getDescendants(POS.PP, POS.CC);

		for(IParseTree ppCCSubTree : ppCCSubTrees) {
			//log(LogLevel.DEBUG, "ppCCSubTree " + ppCCSubTree.getTerminalsText());
			//parseTree.prettyPrint();
			if(chunkCollector.isPartOfChunkType(ppCCSubTree.getTerminals().get(0), ChunkType.COMPARATIVE_VALUE)) //COMPARATIVE_VALUE may constains PPs such as of, than, as-long-as, but they don't belong to PPChunks
				continue;
			IParseTree cc = ppCCSubTree;
			IParseTree pp = ppCCSubTree.getParent(parseTree);
			List<AbstractParseTree> ppChildren = pp.getChildren();
			//all children must be either PP or IN, except for one CC, and
			//all PP child must have a child IN

			boolean isList = true;
			if(!cc.getTerminalsText().matches("and|or")) {
				isList = false;
				//log(LogLevel.DEBUG, "islist false.");
			}
			List<IParseTree> ccs = pp.getChildrenOfPOS(POS.CC);
			if(ccs.size() > 1) {
				isList = false;
				//log(LogLevel.DEBUG, "islist false0");
			}

			int lastin = -1;
			int lastcc = -1;
			int count = 0;
			for(IParseTree child : ppChildren) {
				//log(LogLevel.DEBUG, "child " + child.getTerminalsText());
				if(child.isTerminal())
					continue;
				POS childPOS = child.getPOS();
				if(!(childPOS.equals(POS.PP) || childPOS.equals(POS.IN) || childPOS.equals(POS.CC) ||
						childPOS.equals(POS.NP) || childPOS.equals(POS.TO) || childPOS.equals(POS.ADVP) || childPOS.equals(POS.NONE))) {
					//log(LogLevel.DEBUG, "islist false1");
					isList = false;
				}
				if(childPOS.equals(POS.PP) || childPOS.equals(POS.IN) || childPOS.equals(POS.TO)) {
					lastin = count;
				}
				if(childPOS.equals(POS.CC)) {
					lastcc = count;
				}
				if(childPOS.equals(POS.PP) && child.getChildrenOfPOS(POS.IN).size() == 0) {
					//log(LogLevel.DEBUG, "islist false2");
					isList = false;
				}
				if(childPOS.equals(POS.PP) && child.getChildren().size() > 2){
					//PP is expected to have an IN and an NP as children
					isList = false;
					//log(LogLevel.DEBUG, "islist false3");
				}
				count++;
			}
			if(lastin-lastcc != 1){
				isList = false;
				//log(LogLevel.DEBUG, "islist false4");
			}

			if(isList) {
				//pp.prettyPrint();
				AbstractParseTree newIN = parseTreeFactory.create();
				newIN.setPOS(POS.PREPOSITION);
				AbstractParseTree np = null;
				//collect tokens for the new IN
				for(int i=0; i < ppChildren.size(); i++) {
					AbstractParseTree ppChild = ppChildren.get(i);
					if(ppChild.getPOS().equals(POS.NP)) {
						pp.removeChild(ppChild);
						np = ppChild;
						continue;
					}

					if(i==ppChildren.size()-1) {
						IParseTree lastPPChild = ppChild;
						for(AbstractParseTree lastPPChildChild : lastPPChild.getChildren()) {
							if(lastPPChildChild.getPOS()!=null && lastPPChildChild.getPOS().equals(POS.NP)) {
								np = lastPPChildChild;
							} else {
								newIN.addChildren(lastPPChildChild.getTerminals());
							}
						}
						pp.removeChild(ppChild);
						continue;
					}
					newIN.addChildren(ppChild.getTerminals());;
					pp.removeChild(ppChild);
				}

				pp.addChild(newIN);
				if(np!=null) {
					np.setPOS(POS.OBJECT);
					pp.addChild(np);
				}

				LinkedHashSet<Chunk> ppChildChunks = new LinkedHashSet<Chunk>();

				//form PPList chunk
				LinkedHashSet<Chunk> ppListChildChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : newIN.getTerminals())
					ppListChildChunks.add(terminal);
				Chunk ppListChunk = new Chunk(ChunkType.PP_LIST, ppListChildChunks);
				chunkCollector.addChunk(ppListChunk);
				//form preposition chunk
				Chunk prepositionChunk = new Chunk(ChunkType.PREPOSITION, ppListChunk);
				ppChildChunks.add(prepositionChunk);
				//form Object
				if(np!=null) {
					LinkedHashSet<Chunk> npListChildChunks = new LinkedHashSet<Chunk>();
					for(AbstractParseTree terminal : np.getTerminals()) {
						Chunk previousNPChunk = chunkCollector.getChunk(terminal);
						for(Chunk ppListChildChunk : ppListChildChunks)
							previousNPChunk.removeChunk(ppListChildChunk);
						npListChildChunks.add(previousNPChunk);
					}
					Chunk objectChunk = new Chunk(ChunkType.OBJECT, npListChildChunks);
					ppChildChunks.add(objectChunk);
				}

				Chunk newPPChunk = new Chunk(ChunkType.PP, ppChildChunks);
				chunkCollector.addChunk(newPPChunk);

				//pp.prettyPrint();
			}
		}
	}
}
