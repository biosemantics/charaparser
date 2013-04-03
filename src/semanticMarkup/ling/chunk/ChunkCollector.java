package semanticMarkup.ling.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.log.LogLevel;

/**
 * A ChunkCollector stores the root chunk to which a terminal is assigned to for a single sentence.
 * Furthermore a chunkCollector provides for additional sentence information, such as the sentences parse tree or subject tag.
 * 
 * Note: 
 * Always use ChunkCollectors interface to modify a Chunk. 
 * Or modify chunk and call chunkCollector.add(chunk) afterwards to register the changes.
 * @author rodenhausen
 */
public class ChunkCollector implements Iterable<Chunk> {

	private boolean hasChanged = false;
	private String sentence;
	private AbstractParseTree parseTree;
	private String subjectTag;
	private Treatment treatment;
	//terminalID to Chunk. 
	//IParseTree as key doesn't work as hashcodes created have to depend on parent (otherwise same words in a sentence are assigned same hash)
	//, but IParseTree doesnt know its parent in the current implementation
	private HashMap<Integer, Chunk> chunks = new HashMap<Integer, Chunk>();
	private String source;
	
	/**
	 * @param parseTree
	 * @param subjectTag
	 * @param treatment
	 * @param source
	 * @param sentenceString
	 */
	public ChunkCollector(AbstractParseTree parseTree, String subjectTag, Treatment treatment, String source, String sentenceString) {
		this.parseTree = parseTree;
		/*log(LogLevel.DEBUG, "root before " + parseTree.getClass().getName() + "@" + Integer.toHexString(parseTree.hashCode()));
		for(IParseTree terminal : parseTree.getTerminals()) {
			log(LogLevel.DEBUG, "terminal before " + terminal.getClass().getName() + "@" + Integer.toHexString(terminal.hashCode()));
		}*/
		this.subjectTag = subjectTag;
		this.treatment = treatment;
		this.source = source;
		this.sentence = sentenceString;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("parseTree: ").append(parseTree.toString()).append("\n");
		result.append("subjectTag: ").append(subjectTag).append("\n");
		result.append("source: ").append(source).append("\n");
		result.append("treatment: ").append(treatment.getName()).append("\n");
		result.append("chunks:\n");
		for(AbstractParseTree terminal : getTerminals()) {
			Chunk chunk = this.getChunk(terminal);
			if(chunk!=null)
				result.append(terminal.toString()).append(" => ").append(chunk.toString()).append("\n");
			else
				result.append(terminal.toString()).append(" => ").append("NULL").append("\n");
		}
		//result.append("addOnTerminals: ").append(addOnTerminals).append("\n");
		/*for(IParseTree key : chunks.keySet()) {
			log(LogLevel.DEBUG, key.getClass().getName() + "@" + Integer.toHexString(key.hashCode()));
		}*/
		return result.toString();
	}
	
	/**
	 * @return the sentence
	 */
	public String getSentence() {
		return this.sentence;
	}
	
	/**
	 * @return the corresponding treatment
	 */
	public Treatment getTreatment() {
		return this.treatment;
	}
	
	/**
	 * @return the subject tag
	 */
	public String getSubjectTag() {
		return subjectTag;
	}
	
	/**
	 * @return the parse tree for the sentence
	 */
	public AbstractParseTree getParseTree() {
		return parseTree;
	}

	/**
	 * @param chunk to add
	 */
	public void addChunk(Chunk chunk) {
		if(!(chunk instanceof AbstractParseTree))
			log(LogLevel.DEBUG, "add chunk " + chunk);
				
		//checks for "valid" chunking as of current definition
		List<AbstractParseTree> terminals = chunk.getTerminals();
		int previousTerminalId = getTerminalId(terminals.get(0))-1;
		for(AbstractParseTree terminal : terminals) {
			int currentTerminalId = getTerminalId(terminal);
			if(currentTerminalId-1 != previousTerminalId)
				log(LogLevel.DEBUG, "This is not a valid chunk of consecutive terminals!");
			previousTerminalId = currentTerminalId;
		}
		
		int firstTerminalId = getTerminalId(terminals.get(0));
		int lastTerminalId = getTerminalId(terminals.get(terminals.size()-1));

		if(chunks.containsKey(firstTerminalId - 1)) {
			Chunk previousChunk = chunks.get(firstTerminalId - 1);
			for(AbstractParseTree terminal : chunk.getTerminals()) {
				if(previousChunk.contains(terminal))
					log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in previous chunk");
			}
		}

		if(chunks.containsKey(lastTerminalId + 1)) {
			Chunk nextChunk = chunks.get(lastTerminalId + 1);
			for(AbstractParseTree terminal : chunk.getTerminals()) {
				if(nextChunk.contains(terminal))
					log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in next chunk");
			}
		}
				
		for(IParseTree parseTree : chunk.getTerminals()) {	
			chunks.put(getTerminalId(parseTree), chunk);
		}
		this.hasChanged = true;
	}
	
	/**
	 * @param parseTree
	 * @return the terminal id of the parseTree
	 */
	public int getTerminalId(IParseTree parseTree) {
		return this.getTerminals().indexOf(parseTree);
	}
	
	private int getMaxTerminalId() {
		return this.getTerminals().size() - 1;
	}
	
	/**
	 * @param parseTree
	 * @return the root chunk associated with the parseTree
	 */
	public Chunk getChunk(AbstractParseTree parseTree) {
		if(chunks.get(getTerminalId(parseTree)) == null)
			this.addChunk(parseTree);
		return chunks.get(getTerminalId(parseTree));
	}
	
	/**
	 * @param parseTree
	 * @return the chunkType of the root chunk associated with the parseTree
	 */
	public ChunkType getChunkType(AbstractParseTree parseTree) {
		return this.getChunk(parseTree).getChunkType();
	}

	/**
	 * @param parseTree
	 * @return if parseTree is part of a non terminal chunk 
	 */
	public boolean isPartOfANonTerminalChunk(IParseTree parseTree) {
		int terminalId = getTerminalId(parseTree);
		return chunks.containsKey(terminalId) && !chunks.get(terminalId).isTerminal();
	}
		
	/**
	 * @param parseTree
	 * @param chunkType
	 * @return true if the parseTree participates in a chunk of chunkType
	 */
	public boolean isPartOfChunkType(AbstractParseTree parseTree, ChunkType chunkType) {
		if(chunks.containsKey(getTerminalId(parseTree))) {
			Chunk chunk = this.getChunk(parseTree);
			return chunk.isPartOfChunkType(parseTree, chunkType);
		}
		return false;
	}
	
	/**
	 * @param parseTree
	 * @param chunkType
	 * @return true if the parseTree's root chunk is of chunkType
	 */
	public boolean isOfChunkType(AbstractParseTree parseTree, ChunkType chunkType) {
		if(chunks.containsKey(getTerminalId(parseTree))) {
			Chunk chunk = this.getChunk(parseTree);
			return chunk.isOfChunkType(chunkType);
		}
		return false;
	}
	
	/**
	 * @param parseTrees
	 * @param chunkType
	 * @returns true if any of the parseTrees is part of chunkType
	 */
	public boolean containsPartOfChunkType(List<AbstractParseTree> parseTrees, ChunkType chunkType) {
		for(AbstractParseTree parseTree : parseTrees) {
			if(isPartOfChunkType(parseTree, chunkType))
				return true;
		}
		return false;
	} 
	
	
	/**
	 * @param parseTrees
	 * @param chunkType
	 * @return a chunk of chunkType, in which one of the parseTrees participates. 
	 * If no such chunk exists null is returned.
	 */
	public Chunk getChunkOfChunkType(List<AbstractParseTree> parseTrees, ChunkType chunkType) {
		for(AbstractParseTree parseTree : parseTrees) {
			if(isPartOfChunkType(parseTree, chunkType)) {
				Chunk chunk = this.getChunk(parseTree);
				return chunk.getChunkDFS(chunkType);
			}
		}
		return null;
	}
	

	/**
	 * @return the list of terminals of the sentence
	 */
	public List<AbstractParseTree> getTerminals() {
		return this.parseTree.getTerminals();
	}

	/**
	 * @return the list of root chunk associated with the sentence's terminals. No duplicate chunks are returned.
	 */
	public List<Chunk> getChunks() {
		List<Chunk> chunks = new ArrayList<Chunk>();
		List<AbstractParseTree> terminals = getTerminals();
		Chunk previousChunk = null;
		for(AbstractParseTree terminal : terminals) {
			Chunk chunk = this.getChunk(terminal);
			if(!chunk.equals(previousChunk))
				chunks.add(chunk);
			
			previousChunk = chunk;
		}
		return chunks;
	}
	
	/**
	 * @param chunkType
	 * @return if any of the chunks contains chunkType
	 */
	public boolean containsChunkType(ChunkType chunkType) {
		for(Chunk chunk : this.getChunks()) {
			if(chunk.containsChunkType(chunkType)) 
				return true;
		}
		return false;
	}
	
	/**
	 * @return if the chunkCollector has changed
	 */
	public boolean hasChanged() {
		return this.hasChanged;
	}
	
	/**
	 * reset the has changed flag
	 */
	public void resetHasChanged() {
		this.hasChanged = false;
	}

	@Override
	public Iterator<Chunk> iterator() {
		return new ChunkCollectorIterator<Chunk>(this);
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
}
