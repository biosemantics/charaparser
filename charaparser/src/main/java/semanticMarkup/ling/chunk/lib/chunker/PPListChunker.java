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
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PPListChunker extends AbstractChunker {

	@Inject
	public PPListChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
	}

	/** PP
	 * 	IN on
	 *  CC or    => PP
	 * 	IN above      IN on or above
	 * 	
	 * @param doc
	 * @return
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<AbstractParseTree> ppCCSubTrees = parseTree.getDescendants(POS.PP, POS.CC);
		
		for(IParseTree ppCCSubTree : ppCCSubTrees) {
			//System.out.println("ppCCSubTree " + ppCCSubTree.getTerminalsText());
			//parseTree.prettyPrint();
			IParseTree cc = ppCCSubTree;
			IParseTree pp = ppCCSubTree.getParent(parseTree);
			List<AbstractParseTree> ppChildren = pp.getChildren();
			//all children must be either PP or IN, except for one CC, and
			//all PP child must have a child IN
			
			boolean isList = true;
			if(!cc.getTerminalsText().matches("and|or")) {
				isList = false;
				//System.out.println("islist false.");
			}
			List<IParseTree> ccs = pp.getChildrenOfPOS(POS.CC);
			if(ccs.size() > 1) {
				isList = false;
				//System.out.println("islist false0");
			}
			
			int lastin = -1;
			int lastcc = -1;
			int count = 0;
			for(IParseTree child : ppChildren) {
				//System.out.println("child " + child.getTerminalsText());
				if(child.isTerminal())
					continue;
				POS childPOS = child.getPOS();
				if(!(childPOS.equals(POS.PP) || childPOS.equals(POS.IN) || childPOS.equals(POS.CC) ||
						childPOS.equals(POS.NP) || childPOS.equals(POS.TO) || childPOS.equals(POS.ADVP) || childPOS.equals(POS.NONE))) {
					//System.out.println("islist false1");
					isList = false;
				}
				if(childPOS.equals(POS.PP) || childPOS.equals(POS.IN) || childPOS.equals(POS.TO)) {
					lastin = count;
				}
				if(childPOS.equals(POS.CC)) {
					lastcc = count;
				}
				if(childPOS.equals(POS.PP) && child.getChildrenOfPOS(POS.IN).size() == 0) {
					//System.out.println("islist false2");
					isList = false;
				}
				if(childPOS.equals(POS.PP) && child.getChildren().size() > 2){ 
					//PP is expected to have an IN and an NP as children
					isList = false;
					//System.out.println("islist false3");
				}
				count++;
			}
			if(lastin-lastcc != 1){
				isList = false;
				//System.out.println("islist false4");
			}
			
			if(isList) {
				//pp.prettyPrint();
				AbstractParseTree newIN = parseTreeFactory.create();
				newIN.setPOS(POS.PREPOSITION);
				AbstractParseTree np = null;
				
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
							if(lastPPChildChild.getPOS().equals(POS.NP)) {
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
				
				
				LinkedHashSet<Chunk> ppListChildChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : newIN.getTerminals())
					ppListChildChunks.add(terminal);
				Chunk ppListChunk = new Chunk(ChunkType.PP_LIST, ppListChildChunks);
				chunkCollector.addChunk(ppListChunk);
				
				Chunk prepositionChunk = new Chunk(ChunkType.PREPOSITION, ppListChunk);
				ppChildChunks.add(prepositionChunk);
				
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
