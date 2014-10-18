package edu.arizona.biosemantics.semanticmarkup.ling.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;


/**
 * A ChunkCollector stores the root chunk to which a terminal is assigned to for a single sentence.
 * Furthermore a chunkCollector provides for additional sentence information, such as the sentences parse tree or subject tag.
 * 
 * Note: 
 * Always use ChunkCollectors interface to modify a Chunk. 
 * Or modify chunk and call chunkCollector.add(chunk) afterwards to register the changes.
 * ChunkCollector checks for ill-formed chunks.
 * @author rodenhausen
 */
public class ChunkCollector implements Iterable<Chunk> {

	private boolean hasChanged = false;
	private String sentence;
	private String originalSent;
	private AbstractParseTree parseTree;
	private String subjectTag; //tag field in the sentence table
	private AbstractDescriptionsFile descriptionsFile;
	private Description description; //the entire xml input for a treatment
	private HashMap<IParseTree, Chunk> chunks = new HashMap<IParseTree, Chunk>();
	private String source;
	
	/**
	 * @param parseTree
	 * @param subjectTag
	 * @param treatment
	 * @param source
	 * @param sentenceString
	 */
	public ChunkCollector(AbstractParseTree parseTree, String subjectTag, Description description, AbstractDescriptionsFile descriptionsFile, 
			String source, String sentenceString, String originalSent) {
		this.parseTree = parseTree;
		/*log(LogLevel.DEBUG, "root before " + parseTree.getClass().getName() + "@" + Integer.toHexString(parseTree.hashCode()));
		for(IParseTree terminal : parseTree.getTerminals()) {
			log(LogLevel.DEBUG, "terminal before " + terminal.getClass().getName() + "@" + Integer.toHexString(terminal.hashCode()));
		}*/
		this.subjectTag = subjectTag;
		this.descriptionsFile = descriptionsFile;
		this.description = description;
		this.source = source;
		this.sentence = sentenceString;
		this.originalSent = originalSent;
	}
	
	/*@Override
	public Object clone(){
		return new ChunkCollector((AbstractParseTree)parseTree.clone(), subjectTag, 
				(Description)description.clone(), (AbstractDescriptionsFile) descriptionsFile.clone()
				, source, sentence);
	}*/
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("parseTree: ").append(parseTree.toString()).append("\n");
		result.append("subjectTag: ").append(subjectTag).append("\n");
		result.append("source: ").append(source).append("\n");
		result.append("description: ").append(descriptionsFile.getName()).append("\n");
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
	 * @return the original sentence
	 */
	public String getOriginalSentence() {
		return this.originalSent;
	}
	
	/**
	 * @return the corresponding treatment
	 */
	public Description getDescription() {
		return this.description;
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
		if(chunk.hasCircularReference()) {
			log(LogLevel.ERROR, "A Chunk was about to be added with a circular chunk-chunk reference");
			return;
		}
		if(!(chunk instanceof AbstractParseTree))
			log(LogLevel.DEBUG, "add chunk " + chunk);
				
		//checks for "valid" chunking as of current definition
		List<AbstractParseTree> terminals = chunk.getTerminals();
		int previousTerminalId = getTerminalId(terminals.get(0)) - 1;
		for(AbstractParseTree terminal : terminals) {
			int currentTerminalId = getTerminalId(terminal);
			if(currentTerminalId-1 != previousTerminalId)
				log(LogLevel.DEBUG, "This is not a valid chunk of consecutive terminals!");
			previousTerminalId = currentTerminalId;
		}
		
		int firstTerminalId = getTerminalId(terminals.get(0));
		int lastTerminalId = getTerminalId(terminals.get(terminals.size()-1));
		AbstractParseTree previousTerminal = null;
		AbstractParseTree nextTerminal = null;
		if(firstTerminalId > 0)
			previousTerminal = this.getTerminals().get(firstTerminalId - 1);
		if(lastTerminalId < this.getTerminals().size() - 1)
			nextTerminal = this.getTerminals().get(lastTerminalId + 1);

		if(previousTerminal != null)
			if(chunks.containsKey(previousTerminal)) {
				Chunk previousChunk = chunks.get(previousTerminal);
				for(AbstractParseTree terminal : chunk.getTerminals()) {
					if(previousChunk.containsOrEquals(terminal))
						log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in previous chunk");
				}
			}

		if(nextTerminal != null)
			if(chunks.containsKey(nextTerminal)) {
				Chunk nextChunk = chunks.get(nextTerminal);
				for(AbstractParseTree terminal : chunk.getTerminals()) {
					if(nextChunk.containsOrEquals(terminal))
						log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in next chunk");
				}
			}
				
		for(IParseTree parseTree : chunk.getTerminals()) {	
			chunks.put(parseTree, chunk);
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
		if(chunks.get(parseTree) == null)
			this.addChunk(parseTree);
		return chunks.get(parseTree);
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
		return chunks.get(parseTree) != null && !chunks.get(parseTree).isTerminal();
	}
		
	/**
	 * @param parseTree
	 * @param chunkType
	 * @return true if the parseTree participates in a chunk of chunkType
	 */
	public boolean isPartOfChunkType(AbstractParseTree parseTree, ChunkType chunkType) {
		if(chunks.containsKey(parseTree)) {
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
		if(chunks.containsKey(parseTree)) {
			Chunk chunk = this.getChunk(parseTree);
			return chunk.isOfChunkType(chunkType);
		}
		return false;
	}
	
	/**
	 * @param parseTrees
	 * @param chunkType
	 * @return true if any of the parseTrees is part of chunkType
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