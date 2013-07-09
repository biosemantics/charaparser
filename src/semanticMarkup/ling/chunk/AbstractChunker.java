package semanticMarkup.ling.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AbstractChunker implements common functionality of an IChunker shared among concret IChunker implementations
 * @author rodenhausen
 */
public abstract class AbstractChunker implements IChunker {

	protected IParseTreeFactory parseTreeFactory;
	protected String prepositionWords;
	protected String units;
	protected HashMap<String, String> equalCharacters;
	protected IGlossary glossary;
	protected ITerminologyLearner terminologyLearner;
	protected Set<String> stopWords;
	protected IInflector inflector;
	protected IOrganStateKnowledgeBase organStateKnowledgeBase;
	
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
	public AbstractChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {		
		this.parseTreeFactory = parseTreeFactory;
		this.stopWords = stopWords;
		this.prepositionWords = prepositionWords;
		this.units = units;
		this.equalCharacters = equalCharacters;
		this.glossary = glossary;
		this.terminologyLearner = terminologyLearner;
		this.inflector = inflector;
		this.organStateKnowledgeBase = organStateKnowledgeBase;
	}
		
	protected void collapseSubtree(IParseTree parseTree, IParseTree collapseRoot, POS pos) {
		collapseRoot.setPOS(pos);	
	}
	
	protected AbstractParseTree collapseTwoSubtrees(AbstractParseTree root, POS collapsePOS, IParseTree first, POS firstPOS, IParseTree second, POS secondPOS, ChunkCollector chunkCollector) {	
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
		
		log(LogLevel.DEBUG, "rearranged chunks are reassigned");
		for(Chunk chunk : savedChunks) 
			chunkCollector.addChunk(chunk);
		log(LogLevel.DEBUG, "end of reassignment");
		
		return root;
	}

	protected void createTwoValuedChunk(ChunkType chunkType, AbstractParseTree root, ChunkCollector chunkCollector) {
		Chunk possibleParentChunk = chunkCollector.getChunk(root.getTerminals().get(0));
		boolean parentExists = possibleParentChunk.containsAll(root.getTerminals());

		List<AbstractParseTree> functionTerminals = new ArrayList<AbstractParseTree>();
		List<AbstractParseTree> objectTerminals = new ArrayList<AbstractParseTree>();
		ChunkType functionChunkType = null;
		for(AbstractParseTree child : root.getChildren()) {
			if(!child.getTerminalsText().equals("S") && child.getPOS()!=null) {
				if(child.getPOS().equals(POS.PREPOSITION)) {
					functionTerminals.addAll(child.getTerminals());
					functionChunkType = ChunkType.PREPOSITION;
				}
				if(child.getPOS().equals(POS.VERB)) {
					functionTerminals.addAll(child.getTerminals());
					functionChunkType = ChunkType.VERB;
				}
				if(child.getPOS().equals(POS.OBJECT)) {
					createOrganChunk(child, chunkCollector);
					objectTerminals.addAll(child.getTerminals());
				}
			}
		}
		if(parentExists) {
			Chunk maxFunctionChunk = possibleParentChunk.getMaxDepthChunkThatContainsAButNotB(functionTerminals, objectTerminals);
			Chunk maxObjectChunk =  possibleParentChunk.getMaxDepthChunkThatContainsAButNotB(objectTerminals, functionTerminals);
			//Chunk maxParentChunkFunction = possibleParentChunk.getParentChunk(maxFunctionChunk);
			//Chunk maxParentChunkObject = possibleParentChunk.getParentChunk(maxObjectChunk);
			//Chunk maxCommonParent = possibleParentChunk.getCommonParent(maxParentChunkFunction, maxParentChunkObject);
			
			Chunk oldFunctionChunk = null;
			LinkedHashSet<Chunk> functionChunks = new LinkedHashSet<Chunk>();
			if(maxFunctionChunk != null) {
				oldFunctionChunk = maxFunctionChunk;
				//Chunk functionContent = (Chunk)maxFunctionChunk.clone();
				functionChunks.add(maxFunctionChunk);
			} else {
				oldFunctionChunk = chunkCollector.getChunk(functionTerminals.get(0));
				functionChunks.addAll(functionTerminals);
			}
			Chunk functionChunk = new Chunk(functionChunkType, functionChunks);

			LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
			if(maxObjectChunk != null)
				objectChunks.add(maxObjectChunk);
			else {
				for(AbstractParseTree terminal : objectTerminals) {
					objectChunks.add(possibleParentChunk.getMaxDepthChunkThatContainsOnlyTerminal(terminal));
				}
			}
			Chunk objectChunk = new Chunk(ChunkType.OBJECT, objectChunks);
			
			Chunk twoValuedChunk = new Chunk(chunkType);
			Chunk parentChunk = possibleParentChunk.getParentChunk(oldFunctionChunk);
			LinkedHashSet<Chunk> parentChunkChildren = new LinkedHashSet<Chunk>();
			if(parentChunk!=null) {
				for(Chunk chunk : parentChunk.getChunks()) {
					if(!chunk.equals(oldFunctionChunk) && !chunk.containsAny(objectTerminals))
						parentChunkChildren.add(chunk);
					else if(chunk.equals(oldFunctionChunk))
						parentChunkChildren.add(twoValuedChunk);
				}
				parentChunk.setChunks(parentChunkChildren);
				LinkedHashSet<Chunk> twoValuedChildChunks = new LinkedHashSet<Chunk>();
				twoValuedChildChunks.add(functionChunk);
				twoValuedChildChunks.add(objectChunk);
				twoValuedChunk.setChunks(twoValuedChildChunks);
				chunkCollector.addChunk(possibleParentChunk);
			}
			
		} else {
			LinkedHashSet<Chunk> functionChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree function : functionTerminals) 
				functionChunks.add(chunkCollector.getChunk(function));
			Chunk functionChunk = new Chunk(functionChunkType, functionChunks);
			LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree object : objectTerminals) 
				objectChunks.add(chunkCollector.getChunk(object));
			Chunk objectChunk = new Chunk(ChunkType.OBJECT, objectChunks);
			
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(functionChunk);
			childChunks.add(objectChunk);
			Chunk twoValuedChunk = new Chunk(chunkType, childChunks);
			chunkCollector.addChunk(twoValuedChunk);
		}
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
				boolean alreadyAssignedToValidChunk = false;
				for(AbstractParseTree terminal : terminals) {
					alreadyAssignedToValidChunk |= chunkCollector.isPartOfANonTerminalChunk(terminal);
				}
				if(terminals.size() == 1 && 
						organStateKnowledgeBase.isOrgan(terminals.get(0).getTerminalsText()) && 
						organStateKnowledgeBase.isState(terminals.get(0).getTerminalsText()) && 
						chunkCollector.isPartOfChunkType(terminals.get(0), ChunkType.STATE)) {
					alreadyAssignedToValidChunk = false;
				}

				if(!alreadyAssignedToValidChunk) {
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
			if(child.getTerminalsText().matches("\\p{Punct}"))
				break;
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
