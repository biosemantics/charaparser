package semanticMarkup.ling.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class AbstractChunker implements IChunker {

	protected ParseTreeFactory parseTreeFactory;
	protected String prepositionWords;
	protected String units;
	protected HashMap<String, String> equalCharacters;
	protected IGlossary glossary;
	protected ITerminologyLearner terminologyLearner;
	protected Set<String> stopWords;
	protected IInflector inflector;
	
	@Inject
	public AbstractChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector) {		
		this.parseTreeFactory = parseTreeFactory;
		this.stopWords = stopWords;
		this.prepositionWords = prepositionWords;
		this.units = units;
		this.equalCharacters = equalCharacters;
		this.glossary = glossary;
		this.terminologyLearner = terminologyLearner;
		this.inflector = inflector;
	}
		
	
	protected void collapseSubtree(IParseTree parseTree, IParseTree collapseRoot, POS pos) {
		collapseRoot.setPOS(pos);
		
	}
	
	
	protected IParseTree collapseTwoSubtrees(IParseTree root, POS collapsePOS, IParseTree first, POS firstPOS, IParseTree second, POS secondPOS, ChunkCollector chunkCollector) {	
		//terminalIds may change due to rearrangement in parseTree. Hence chunks need to be saved and reassigned.
		Set<Chunk> savedChunks = new HashSet<Chunk>();
		for(AbstractParseTree terminal : root.getTerminals()) {
			savedChunks.add(chunkCollector.getChunk(terminal));
		}
		
		first.setPOS(firstPOS);
		IParseTree secondParent = second.getParent(root);
		secondParent.removeChild(second);
		int firstIndex = root.indexOf(first);
		root.addChild(firstIndex+1, second);
		second.setPOS(secondPOS);
		
		root.setPOS(collapsePOS);
		
		System.out.println("rearranged chunks are reassigned");
		for(Chunk chunk : savedChunks) 
			chunkCollector.addChunk(chunk);
		System.out.println("end of reassignment");
		
		return root;
	}
	
	protected Chunk createTwoValuedChunk(ChunkType chunkType, IParseTree root, ChunkCollector chunkCollector) {
		root.prettyPrint();
		
		Chunk functionChunk = null;
		Chunk objectChunk = null;
		for(AbstractParseTree child : root.getChildren()) {
			if(child.getPOS().equals(POS.PREPOSITION)) {
				LinkedHashSet<Chunk> terminalChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : child.getTerminals()) {
					terminalChunks.add(chunkCollector.getChunk(terminal));
				}
				functionChunk = new Chunk(ChunkType.PREPOSITION, terminalChunks);
				chunkCollector.addChunk(functionChunk);
			}
			if(child.getPOS().equals(POS.VERB)) {
				LinkedHashSet<Chunk> terminalChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : child.getTerminals()) {
					terminalChunks.add(chunkCollector.getChunk(terminal));
				}
				functionChunk = new Chunk(ChunkType.VERB, terminalChunks);
				chunkCollector.addChunk(functionChunk);
			}
			if(child.getPOS().equals(POS.OBJECT)) {
				createOrganChunk(child, chunkCollector);
				LinkedHashSet<Chunk> terminalChunks = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : child.getTerminals()) {
					terminalChunks.add(chunkCollector.getChunk(terminal));
				}
				objectChunk = new Chunk(ChunkType.OBJECT, terminalChunks);
				chunkCollector.addChunk(objectChunk);
				break;
			}
		}
		
		Chunk chunk = null;
		if(functionChunk != null && objectChunk != null) {
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(functionChunk);
			childChunks.add(objectChunk);
			chunk = new Chunk(chunkType, childChunks);
		}
		
		return chunk;
	}
	
	
	private void createOrganChunk(AbstractParseTree child, ChunkCollector chunkCollector) {
		if(!chunkCollector.containsPartOfChunkType(child.getChildren(), ChunkType.ORGAN)) {
			Set<POS> nounPOS = new HashSet<POS>();
			nounPOS.add(POS.NP);
			nounPOS.add(POS.NNS);
			nounPOS.add(POS.NNPS);
			nounPOS.add(POS.NN);
			nounPOS.add(POS.NNP);
			IParseTree firstNPTree = this.getFirstTree(nounPOS, child, chunkCollector);
			if(firstNPTree!=null) {
				List<AbstractParseTree> terminals = firstNPTree.getTerminals();
				boolean alreadyAssignedToChunk = false;
				for(AbstractParseTree terminal : terminals) {
					alreadyAssignedToChunk |= chunkCollector.isPartOfANonTerminalChunk(terminal);
				}
				if(!alreadyAssignedToChunk) {
					List<Chunk> children = new ArrayList<Chunk>(firstNPTree.getTerminals());
					chunkCollector.addChunk(new Chunk(ChunkType.ORGAN, children));
				}
			}
		}
	}


	protected IParseTree getFirstTree(Set<POS> pos, AbstractParseTree parseTree, ChunkCollector chunkCollector) {
		IParseTree result = null;
		if(!parseTree.isTerminal() && pos.contains(parseTree.getPOS())) {
			result = parseTree;
		}
		
		for(AbstractParseTree child : parseTree.getChildren()) {
			IParseTree childResult = getFirstTree(pos, child, chunkCollector);
			if(childResult != null) {
				result = childResult;
				break;
			}
		}
		
		return result;
	}
	
	protected IParseTree getFirstObjectTree(AbstractParseTree parseTree, AbstractParseTree afterThisTree, ChunkCollector chunkCollector) {
		LinkedList<AbstractParseTree> result = new LinkedList<AbstractParseTree>();
		getFirstTree(parseTree, afterThisTree, chunkCollector, result, false);
		if(result.isEmpty())
			return null;
		return result.get(result.size()-1);
	}
	
	private boolean getFirstTree(AbstractParseTree parseTree, AbstractParseTree afterThisTree, ChunkCollector chunkCollector, LinkedList<AbstractParseTree> result, boolean after) {
		Set<POS> lastSubtreePOS = new HashSet<POS>();
		lastSubtreePOS.add(POS.NP);
		lastSubtreePOS.add(POS.OBJECT);
		Set<POS> firstSubtreePOS = new HashSet<POS>();
		firstSubtreePOS.add(POS.PP);
		firstSubtreePOS.add(POS.COLLAPSED_PPIN);
		firstSubtreePOS.add(POS.COLLAPSED_VB);
		firstSubtreePOS.add(POS.VP);
		
		if(parseTree.equals(afterThisTree))
			after = true;
		
		if(after) {
			if(!parseTree.isTerminal()) 
				if(firstSubtreePOS.contains(parseTree.getPOS())) {
					result.add(parseTree);
					return after;
				} else if(lastSubtreePOS.contains(parseTree.getPOS())) {
					result.add(parseTree);
				}
		}
		
		for(AbstractParseTree child : parseTree.getChildren()) {
			LinkedList<AbstractParseTree> childResult = new LinkedList<AbstractParseTree>();
			after = getFirstTree(child, afterThisTree, chunkCollector, childResult, after);
			if(!childResult.isEmpty()) {
				result.clear();
				result.addAll(childResult);
				break;
			}
		}
		
		return after;
	}
	
	
	@Override
	public String getName() {
		return this.getClass().toString();
	}
}
