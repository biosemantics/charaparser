package semanticMarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * OrganRecoverChunker attempts to mark modified non-subject organs as a chunk
 * @author rodenhausen
 */
public class OrganRecoverChunker extends AbstractChunker {

	private IOrganStateKnowledgeBase organStateKnowledgeBase;

	@Inject
	public OrganRecoverChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
		this.organStateKnowledgeBase = organStateKnowledgeBase;
	}

	/**
	 * attempts to mark modified non-subject organs as a chunk to avoid characters of these organs be attached to previous organs
	 * run this after recoverConjunctedOrgans to exclude organs that are objects of VP/PP-phrases)
	 * does not attempt to recognize conjunctions as the decisions may be context-sensitive
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		
		boolean previousOrgan = false;
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			//AbstractParseTree nextTerminal = null;
			//if(i<terminals.size()-2)
			//	nextTerminal = terminals.get(i+1);
			
			if(chunkCollector.isOfChunkType(terminal, ChunkType.CONSTRAINT) && !previousOrgan) { 
				if(organStateKnowledgeBase.isOrgan(terminal.getTerminalsText())) {
					Chunk organ = new Chunk(ChunkType.ORGAN, terminal);
					chunkCollector.addChunk(organ);
					previousOrgan = true;
					continue;
				}
			}
			
			if(chunkCollector.isOfChunkType(terminal, ChunkType.CONSTRAINT) && previousOrgan) {
				continue;
			}
			
			if(chunkCollector.isOfChunkType(terminal, ChunkType.ORGAN) ||
					chunkCollector.isPartOfChunkType(terminal, ChunkType.MAIN_SUBJECT_ORGAN) ||
					chunkCollector.isPartOfChunkType(terminal, ChunkType.NON_SUBJECT_ORGAN)) {
				previousOrgan = true;
				continue;
			}
			previousOrgan = false;
		}
		
		/*for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			AbstractParseTree nextTerminal = null;
			if(i<terminals.size()-2)
				nextTerminal = terminals.get(i+1);
			if(chunkCollector.isOfChunkType(terminal, ChunkType.CONSTRAINT) && (nextTerminal==null || 
					!(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.ORGAN) ||
					chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.MAIN_SUBJECT_ORGAN) ||
					chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.NON_SUBJECT_ORGAN)))) {
				if(organStateKnowledgeBase.isOrgan(terminal.getTerminalsText())) {
					Chunk organ = new Chunk(ChunkType.ORGAN, terminal);
					chunkCollector.addChunk(organ);
				}
			}
		}*/
		
		int firstOrgan = -1;
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
			//if(chunkCollector.isOfChunkType(terminal, ChunkType.ORGAN)) {
				firstOrgan = i;
				break;
			}
		}
		
		Chunk previousChunk = null;;
		boolean connectChunks = false;
		AbstractParseTree connector = null;
		for(int i = terminals.size()-1; i >=0; i--){
			AbstractParseTree terminal = terminals.get(i);
			
			Chunk organChunk = null;
			if(chunkCollector.isOfChunkType(terminal, ChunkType.ORGAN) || chunkCollector.isOfChunkType(terminal, ChunkType.NP_LIST)) {
				//TODO: not dealing with nplist at this time, may be later
				organChunk = recoverOrgan(terminals, i, firstOrgan, chunkCollector);//chunk and update chunkedtokens
				int organChunkTerminals = organChunk.getTerminals().size();
				
				if(connectChunks && previousChunk != null) {
					LinkedHashSet<Chunk> childChunks = organChunk.getChunks();
					childChunks.add(connector);
					childChunks.addAll(previousChunk.getChunks());
					organChunk.setChunks(childChunks);
					chunkCollector.addChunk(organChunk);
				}
				previousChunk = organChunk;
				
				if(i>=organChunkTerminals) {
					connector = terminals.get(i-organChunkTerminals);
					if(connector.getTerminalsText().equals("or") || connector.getTerminalsText().equals("and")) {
						connectChunks = true;
					} else 
						connectChunks = false;
				}
			} else if(previousChunk!=null && !previousChunk.contains(terminal) && !terminal.equals(connector)){
				connectChunks = false;
			}
		}	
	}
	
	/**
	 * 
	 * @param firstOrgan 
	 * @param last: the index of the last part of an organ name
	 */
	private Chunk recoverOrgan(List<AbstractParseTree> terminals, int last, int firstOrgan, ChunkCollector chunkCollector) {
		AbstractParseTree terminal = terminals.get(last);
		LinkedHashSet<Chunk> collectedTerminals = new LinkedHashSet<Chunk>();
		collectedTerminals.add(chunkCollector.getChunk(terminal));
		
		//String chunk = this.chunkedtokens.get(last);
		boolean foundModifier = false; //modifiers
		boolean subjectOrgan = false;
		boolean lastCollectedModifier = false;
		int i = last - 1;
		for(;i >= 0; i--){
			AbstractParseTree lookBehindTerminal = terminals.get(i);
			/*preventing "the" from blocking the organ following ",the" to being matched as a subject organ- mohan 10/19/2011*/
			if(lookBehindTerminal.getTerminalsText().matches("the|a|an")){
				if(i != 0) {
					i--;
					lookBehindTerminal = terminals.get(i);
				}
			}
			/*end mohan*/
			if(chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.CONSTRAINT) ||
					chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.MODIFIER) ||
					chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.CHARACTER_STATE) || 
					chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.STATE) || 
					lookBehindTerminal.getTerminalsText().contains("~list~")){
				if(chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.MODIFIER))
					lastCollectedModifier = true;
				else
					lastCollectedModifier = false;
				
				List<Chunk> collectedTerminalsList = new ArrayList<Chunk>(collectedTerminals);
				collectedTerminals.clear();
				Chunk chunk = chunkCollector.getChunk(lookBehindTerminal);
				if(chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.MODIFIER) ||
						chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.STATE)) {
					chunk.setChunkType(ChunkType.CONSTRAINT);
				}
				collectedTerminals.add(chunkCollector.getChunk(lookBehindTerminal));
				collectedTerminals.addAll(collectedTerminalsList);
				foundModifier = true;
			} else if(!foundModifier && (chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.ORGAN) || 
					chunkCollector.isOfChunkType(lookBehindTerminal, ChunkType.NP_LIST))) {
				//if m o m o, collect two chunks
				List<Chunk> collectedTerminalsList = new ArrayList<Chunk>(collectedTerminals);
				collectedTerminals.clear();
				collectedTerminals.add(chunkCollector.getChunk(lookBehindTerminal));
				collectedTerminals.addAll(collectedTerminalsList);
				lastCollectedModifier = false;
			} else {
				if(lookBehindTerminal.getTerminalsText().equals(",") && !lastCollectedModifier) 
					subjectOrgan = true;
				break;
			}
		}
		
		//reformat this.chunkedtokens
		
		Chunk chunk;
		Chunk collectedTerminalsChunk = new Chunk(ChunkType.UNASSIGNED, collectedTerminals);
		if(subjectOrgan || i == firstOrgan - collectedTerminalsChunk.getTerminals().size() || i == -1) { 
			chunk = new Chunk(ChunkType.MAIN_SUBJECT_ORGAN, collectedTerminals);
			chunkCollector.addChunk(chunk);
		} else {
			chunk = new Chunk(ChunkType.NON_SUBJECT_ORGAN, collectedTerminals);
			chunkCollector.addChunk(chunk);
		}
		return chunk;
	}
}
