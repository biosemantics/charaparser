package edu.arizona.biosemantics.semanticmarkup.ling.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
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
 * 
 * 
 * holdes the parse tree
 * then aslo the chunks created by ChunkerChain.
 */
public class ChunkCollector implements Iterable<Chunk> {

	private boolean hasChanged = false;
	private String sentence;
	private AbstractParseTree parseTree;
	private String subjectTag; //tag field in the sentence table
	private AbstractDescriptionsFile descriptionsFile;
	private Description description; //the entire xml input for a treatment
	//terminalID to Chunk. 
	//IParseTree as key doesn't work as hashcodes created have to depend on parent (otherwise same words in a sentence are assigned same hash)
	//, but IParseTree doesnt know its parent in the current implementation
	private HashMap<Integer, Chunk> chunks = new HashMap<Integer, Chunk>(); //integer is the index of ther terminal nodes in the parse tree
	private String source;
	private HashMap<Integer, Integer> indexConverter = new HashMap<Integer, Integer>(); //used when accessing chunks via parsetree.
	
	/**
	 * @param parseTree
	 * @param subjectTag
	 * @param treatment
	 * @param source
	 * @param sentenceString
	 */
	public ChunkCollector(AbstractParseTree parseTree, String subjectTag, Description description, AbstractDescriptionsFile descriptionsFile, 
			String source, String sentenceString) {
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
		//init indexConverter
		int i = 0;
		for(AbstractParseTree node: getTerminals()){
			indexConverter.put(i, i);
			i++;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("parseTree: ").append(parseTree.toString()).append("\n");
		result.append("subjectTag: ").append(subjectTag).append("\n");
		result.append("source: ").append(source).append("\n");
		result.append("description: ").append(descriptionsFile.getName()).append("\n");
		result.append("chunks:\n");
		for(AbstractParseTree terminal : getTerminals()) {
			Chunk chunk = this.getChunk(terminal); //this is problematics to be inculded in toString function.
			//Chunk chunk = chunks.get(getConvertedTerminalId(terminal));
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
				if(previousChunk.containsOrEquals(terminal))
					log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in previous chunk");
			}
		}

		if(chunks.containsKey(lastTerminalId + 1)) {
			Chunk nextChunk = chunks.get(lastTerminalId + 1);
			for(AbstractParseTree terminal : chunk.getTerminals()) {
				if(nextChunk.containsOrEquals(terminal))
					log(LogLevel.DEBUG, "This is not a valid chunk. Terminal was already included in next chunk");
			}
		}
				
		for(IParseTree parseTree : chunk.getTerminals()) {	
			//chunks.put(getTerminalId(parseTree), chunk); //first check if the index exists in the chunks!
			chunks.put(getConvertedTerminalId(parseTree), chunk);
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
	
	public int getConvertedTerminalId(IParseTree parseTree) {
		Integer i = indexConverter.get(getTerminals().indexOf(parseTree));
		if(i==null){
			System.out.println();
			log(LogLevel.ERROR, "Can't located '"+parseTree.toString()+"' in sentence: "+sentence);
		}
		return i;
		//return this.getTerminals().indexOf(parseTree);
	}
	
	/*private int getMaxTerminalId() {
		return this.getTerminals().size() - 1; //note about indexConverter if bring this to life
	}*/
	
	/**
	 * @param parseTree
	 * @return the root chunk associated with the parseTree
	 */
	public Chunk getChunk(AbstractParseTree parseTree) {
		if(chunks.get(getConvertedTerminalId(parseTree)) == null)
			this.addChunk(parseTree);
		return chunks.get(getConvertedTerminalId(parseTree));
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

	/**
	 * update indexConverter
	 * call this method right after a newnode is added to terminal
	 * @param terminal
	 * @param toTree
	 */
	public void reindex(AbstractParseTree terminal, AbstractParseTree newnode) {
		//what if the parsetree have other nodes same as newnode?
		int i = getTerminalId(newnode.getTerminals().get(0));
		int n = newnode.getTerminals().size();
		for(int j = 0; j< n; j++){
			for(int k=getTerminals().size()-1; k >= i+j+1;  k--){
				Integer t = this.indexConverter.get(k-1);
				this.indexConverter.put(k, t);//make room for the new element
			}
			//this.indexConverter.put(i+j, this.indexConverter.get(i-1)); //associate all new s with the original index of the point of addition 
			this.indexConverter.put(i+j, this.indexConverter.get(i==0? 0: i-1)); //associate all new s with the original index of the point of addition 
		}

		
	}
}
