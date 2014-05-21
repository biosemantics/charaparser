package edu.arizona.biosemantics.semanticmarkup.ling.chunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;


/**
 * A Chunk has a ChunkType, contains a ordered set of children chunks and may have properties.
 * A AbstractParseTree constitutes a terminal Chunk.
 * @author rodenhausen
 */
public class Chunk implements Cloneable {

	private ChunkType chunkType;
	private LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
	private HashMap<String, String> properties = new HashMap<String, String>(); 
	
	/**
	 * @param chunkType
	 */
	public Chunk(ChunkType chunkType) {
		this.chunkType = chunkType;
	}
	
	/**
	 * @return the size of the chunk, i.e. the number of children chunks
	 */
	public int size() {
		return chunks.size();
	}
	
	/**
	 * @param chunkType
	 * @param chunk
	 */
	public Chunk(ChunkType chunkType, Chunk chunk) {
		this.chunkType = chunkType;
		this.chunks.add(chunk);
	}
	
	/**
	 * @param chunkType
	 * @param chunks
	 */
	public Chunk(ChunkType chunkType, Collection<Chunk> chunks) {
		this.chunkType = chunkType;
		for(Chunk chunk : chunks)
			this.chunks.add(chunk);
	}
	
	/**
	 * @return this chunks chunkType
	 */
	public ChunkType getChunkType() {
		return chunkType;
	}

	/**
	 * @param chunkType to set
	 */
	public void setChunkType(ChunkType chunkType) {
		this.chunkType = chunkType;
	}
	
	/**
	 * @param chunks to set
	 */
	public void setChunks(LinkedHashSet<Chunk> chunks) {
		this.chunks = chunks;
	}

	/**
	 * @return the child chunks
	 */
	public LinkedHashSet<Chunk> getChunks() {
		return this.chunks;
	}
	
	/**
	 * @return the terminals of this chunk
	 */
	public List<AbstractParseTree> getTerminals() {
		ArrayList<AbstractParseTree> result = new ArrayList<AbstractParseTree>();
		if(this instanceof AbstractParseTree)
			result.add((AbstractParseTree)this);
		else
			for(Chunk chunk : chunks) {
				result.addAll(chunk.getTerminals());
			}
		return result;
	}
	
	/**
	 * @return if this chunk is of ChunkType.UNASSIGNED
	 */
	public boolean isUnassigned() {
		return this.isOfChunkType(ChunkType.UNASSIGNED);
	}
	
	/**
	 * @param chunkType
	 * @return if this chunk is of chunkType
	 */
	public boolean isOfChunkType(ChunkType chunkType) {
		return this.chunkType.equals(chunkType);
	}
	
	/**
	 * @return the text of the terminals
	 */
	public String getTerminalsText() {
		StringBuilder result = new StringBuilder();
		for(AbstractParseTree terminal : getTerminals()) 
			result.append(terminal.getTerminalsText()).append(" ");
		return result.toString().trim();
	}
	
	/**
	 * @param chunkType
	 * @return if this contains a child of chunkType
	 */
	public boolean containsChildOfChunkType(ChunkType chunkType) {
		for(Chunk chunk : chunks) {
			if(chunk.getChunkType().equals(chunkType))
				return true;
		}
		return false;
	}
	
	/**
	 * @param chunkType
	 * @return if this contains a chunk descendant of chunkType
	 */
	public boolean containsChunkType(ChunkType chunkType) {
		if(this.chunkType.equals(chunkType))
			return true;
		for(Chunk chunk : chunks) 
			if(chunk.containsChunkType(chunkType)) {
				return true;
			}
		return false;
	}
	
	/**
	 * @param parseTree
	 * @param chunkType
	 * @return if parseTree is part of this chunk and further is part of chunkType
	 */
	public boolean isPartOfChunkType(AbstractParseTree parseTree,
			ChunkType chunkType) {
		if(this.chunkType.equals(chunkType) && this.getTerminals().contains(parseTree))
			return true;
		for(Chunk chunk : chunks) 
			if(chunk.isPartOfChunkType(parseTree, chunkType)) {
				return true;
			}
		return false;
	}

	/**
	 * @param chunkType
	 * @return first chunk of this chunkType in depth first search manner
	 */
	public Chunk getChunkDFS(ChunkType chunkType) {
		if(this.getChunkType().equals(chunkType)) 
			return this;
		else 
			for(Chunk chunk : chunks) {
				Chunk result = chunk.getChunkDFS(chunkType);
				if(result!=null)
					return result;
			}
		return null;
	}
	
	/**
	 * @param chunkType
	 * @return first chunk of chunkType in breadth first search manner
	 */
	public Chunk getChunkBFS(ChunkType chunkType) {
		LinkedList<Chunk> searchQueue = new LinkedList<Chunk>();
		searchQueue.offer(this);
		Chunk searchChunk = null;
		while((searchChunk = searchQueue.poll())!=null) {
			if(searchChunk.isOfChunkType(chunkType))
				return searchChunk;
			else
				searchQueue.addAll(searchChunk.getChunks());
		}
		return null;
	}
	
	/**
	 * @param chunkType
	 * @return list of all decendants of the chunkType
	 */
	public List<Chunk> getChunks(ChunkType chunkType) {
		List<Chunk> result = new ArrayList<Chunk>();
		if(this.getChunkType().equals(chunkType)) {
			result.add(this);
		} else {
			for(Chunk chunk : chunks) {
				result.addAll(chunk.getChunks(chunkType));
			}
		}
		return result;
	}
	
	/**
	 * @param chunkType
	 * @return first chunk of chunkType in children
	 */
	public Chunk getChildChunk(ChunkType chunkType) {
		for(Chunk chunk : chunks) {
			if(chunk.getChunkType().equals(chunkType)) {
				return chunk;
			}
		}
		return null;
	}
	
	/**
	 * @param key
	 * @return the property value of the property specified by key
	 */
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	/**
	 * @param key
	 * @param value to set for the property specified by key
	 */
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	/**
	 * clears the properties
	 */
	public void clearProperties() {
		this.properties.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(chunkType).append(": ");
		for(Entry<String, String> entry : this.properties.entrySet()) 
			result.append(entry.getKey() + "->" + entry.getValue() + "; ");
		result.append(chunks.toString());
		return result.toString().trim();
	}
	
	/** 
	 * @param key
	 * @return the first property specified by key in all descendant chunks searched in depth first search
	 */
	public String getPropertyDFS(String key) {
		if(this.properties.containsKey(key)) 
			return properties.get(key);
		else 
			for(Chunk chunk : chunks) {
				String result = chunk.getPropertyDFS(key);
				if(result!=null)
					return result;
			}
		return null;
	}
	
	/** 
	 * @param key
	 * @return the first property specified by key in all descendant chunks searched in breadth first search
	 */
	public String getPropertyBFS(String key) {
		LinkedList<Chunk> searchQueue = new LinkedList<Chunk>();
		searchQueue.offer(this);
		Chunk searchChunk = null;
		while((searchChunk = searchQueue.poll())!=null) {
			if(searchChunk.properties.containsKey(key))
				return searchChunk.properties.get(key);
			else
				searchQueue.addAll(searchChunk.getChunks());
		}
		return null;
	}
	
	/**
	 * @param chunk
	 * @return the number of child chunks left in this chunk
	 */
	public int removeChunk(Chunk chunk) {
		//log(LogLevel.DEBUG, this + " removeChunk " + chunk);
		if(chunks.contains(chunk))
			chunks.remove(chunk);
		else {
			Iterator<Chunk> chunkIterator = chunks.iterator();
			while(chunkIterator.hasNext()) {
				Chunk childChunk = chunkIterator.next();
				if(!childChunk.isTerminal()) {
					int leftChildren = childChunk.removeChunk(chunk);
					if(leftChildren == 0) {
						//log(LogLevel.DEBUG, "remove " + childChunk);
						chunkIterator.remove();
					}
				}
			}
		}
		return chunks.size();
	}
	
	/**
	 * removes all child chunks
	 */
	public void clearChunks() {
		this.chunks.clear();
	}
	
	/**
	 * @param chunk
	 * @return if chunk is contained as a descendant
	 */
	public boolean containsOrEquals(Chunk chunk) {
		if(this.equals(chunk))
			return true;
		if(chunks.contains(chunk))
			return true;
		else {
			for(Chunk childChunk : chunks) {
				if(childChunk.containsOrEquals(chunk))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @param chunk
	 * @return if chunk is contained as a descendant
	 */
	public boolean contains(Chunk chunk) {
		if(chunks.contains(chunk))
			return true;
		else {
			for(Chunk childChunk : chunks) {
				if(childChunk.contains(chunk))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @param terminal
	 * @return the chunk at maximal depth, which contains only terminal
	 */
	public Chunk getMaxDepthChunkThatContainsOnlyTerminal(AbstractParseTree terminal) {
		List<AbstractParseTree> terminals = this.getTerminals();
		if(terminals.size()==1 && terminals.contains(terminal)) {
			return this;
		} else if(terminals.contains(terminal)) {
			for(Chunk chunk : this.chunks) {
				Chunk maxChunk = chunk.getMaxDepthChunkThatContainsOnlyTerminal(terminal);
				if(maxChunk!=null)
					return maxChunk;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param terminalsA
	 * @param terminalsB
	 * @return the chunk at maximal depth, which contains terminalsA but not terminalsB
	 */
	public Chunk getMaxDepthChunkThatContainsAButNotB(Collection<AbstractParseTree> terminalsA, Collection<AbstractParseTree> terminalsB) {
		if(this.containsAll(terminalsA) && !this.containsAny(terminalsB)) {
			return this;
		} else {
			for(Chunk child : this.getChunks()) {
				Chunk result = child.getMaxDepthChunkThatContainsAButNotB(terminalsA, terminalsB);
				if(result != null)
					return result;
			}
		}
		return null;
	}
	
	/**
	 * @param terminals
	 * @return if all terminals are contained in this chunk
	 */
	public boolean containsAll(Collection<AbstractParseTree> terminals) {
		boolean result = true;
		for(AbstractParseTree terminal : terminals) {
			result &= this.containsOrEquals(terminal);
			if(!result)
				return result;
		}
		return result;
	}
	
	/**
	 * @param terminals
	 * @return if any of the terminals is contained in this chunk
	 */
	public boolean containsAny(Collection<AbstractParseTree> terminals) {
		boolean result = false;
		for(AbstractParseTree terminal : terminals) {
			result |= this.containsOrEquals(terminal);
			if(result)
				return result;
		}
		return result;
	}
	
	/**
	 * @param chunk
	 * @return the parent chunk of the given chunk or null if is is not contained in this chunk
	 */
	public Chunk getParentChunk(Chunk chunk) {
		if(this.containsAsChild(chunk))
			return this;
		else
			for(Chunk child : chunks) {
				Chunk parentChunk = child.getParentChunk(chunk);
				if(parentChunk != null)
					return parentChunk;
			}
		return null;
	}
	
	/**
	 * @param chunkA
	 * @param chunkB
	 * @return the common ancestor of maximal depth, which contains both chunkA and chunkB 
	 */
	public Chunk getCommonAncestor(Chunk chunkA, Chunk chunkB) {
		if(chunkA.containsOrEquals(chunkB)) {
			return chunkA;
		}
		if(chunkB.containsOrEquals(chunkA)) {
			return chunkB;
		}
		Chunk parentA = this.getParentChunk(chunkA);
		Chunk result = this.getCommonAncestor(parentA, chunkB);
		if(result != null)
			return result;
		
		Chunk parentB = this.getParentChunk(chunkB);
		result = this.getCommonAncestor(parentA, chunkB);
		if(result != null)
			return result;
		
		result = this.getCommonAncestor(parentA, parentB);
		return result;
	}
	
	/**
	 * @param chunk
	 * @return if chunk is contained as child
	 */
	public boolean containsAsChild(Chunk chunk) {
		return chunks.contains(chunk);
	}
	
	/**
	 * @param chunks to remove
	 */
	public void removeChunks(Collection<Chunk> chunks) {
		for(Chunk chunk : chunks)
			this.removeChunk(chunk);
	}
	
	/**
	 * @return if this chunk is a terminal chunk
	 */
	public boolean isTerminal() {
		return this instanceof AbstractParseTree;
	}

	/**
	 * @param terminal
	 * @return list of children chunks that appear in the children after the chunk which contains terminal
	 */
	public List<Chunk> getChunksIncludingAfterTerminal(AbstractParseTree terminal) {
		List<Chunk> result = new LinkedList<Chunk>();
		for(Chunk chunk : this.getChunks()) {
			if(chunk.containsOrEquals(terminal)) {
				result.addAll(chunk.getChunksIncludingAfterTerminal(terminal));
			} else if(chunk.equals(terminal)) {
				result.add(terminal);
			} else if(!result.isEmpty()) 
				result.add(chunk);
		}
		return result;
	}

	/**
	 * @param chunkType
	 * @param ofTerminal
	 * @return descendant chunk that is of chunkType and contains ofTerminal
	 */
	public Chunk getChunkOfTypeAndTerminal(ChunkType chunkType, AbstractParseTree ofTerminal) {
		Chunk result = null;
		if(this.getChunkType().equals(chunkType) && this.containsOrEquals(ofTerminal)) {
			result = this;
		} else {
			for(Chunk chunk : chunks) {
				result = chunk.getChunkOfTypeAndTerminal(chunkType, ofTerminal);
				if(result != null)
					break;
			}
		}
		return result;
	}
	
	/**
	 * @param ofTerminal
	 * @return descendant chunk that contains ofTerminal
	 */
	public Chunk getChildChunkOfTerminal(AbstractParseTree ofTerminal) {
		for(Chunk chunk : chunks) {
			if(chunk.containsOrEquals(ofTerminal))
				return chunk;
		}
		return null;
	}

	/**
	 * @param chunk to insert. Descendant chunk of this which is of maximal depth and contains the first terminal of given chunk
	 * will be replaced by given chunk.
	 */
	public void addAndReplaceChunks(Chunk chunk) {
		AbstractParseTree firstTerminal = chunk.getTerminals().get(0);
		for(Chunk child : chunks) {
			if(child.containsOrEquals(firstTerminal)) {
				child.addAndReplaceChunks(chunk);
				break;
			}
			if(child.equals(firstTerminal)) {
				LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
				for(Chunk childChunk : chunks) {
					if(chunk.containsOrEquals(childChunk))
						newChunks.add(chunk);
					else 
						newChunks.add(childChunk);
				}
				chunks = newChunks;
				break;
			}
		}
	}
	
	/**
	 * @param terminal
	 * @return minimal depth descendant chunks that do not contain terminal 
	 */
	public List<Chunk> getChunksWithoutTerminal(AbstractParseTree terminal) {
		List<Chunk> result = new LinkedList<Chunk>();
		for(Chunk chunk : this.getChunks()) {
			if(chunk.containsOrEquals(terminal) || chunk.equals(terminal)) {
				result.addAll(chunk.getChunksWithoutTerminal(terminal));
			} else
				result.add(chunk);
		}
		return result;
	}
	
	@Override 
	public Object clone() {
		try {
			Chunk clone = (Chunk)super.clone();
			clone.chunkType = this.getChunkType();
			clone.properties = new HashMap<String, String>();
			for(Entry<String, String> entry : this.properties.entrySet()) {
				clone.properties.put(entry.getKey(), entry.getValue());
			}
			clone.chunks = new LinkedHashSet<Chunk>();
			for(Chunk chunk : this.chunks) {
				clone.chunks.add((Chunk)chunk.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, "Problem cloning Chunk", e);
		}
		return null;
	}

	/**
	 * @param toReplaceChunk is removed
	 * @param replaceChunk is inserted
	 * @return if a chunk has been replaced
	 */
	public boolean replaceChunk(Chunk toReplaceChunk, Chunk replaceChunk) {
		for(Chunk chunk : this.chunks) {
			if(chunk.equals(toReplaceChunk)) {
				LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
				for(Chunk oldChunk : this.chunks) {
					if(oldChunk.equals(toReplaceChunk))
						newChunks.add(replaceChunk);
					else
						newChunks.add(oldChunk);
				}
				this.chunks = newChunks;
				return true;
			}
			else {
				boolean result = chunk.replaceChunk(toReplaceChunk, replaceChunk);
				if(result)
					return true;
			}
		}
		return false;
	}

	public boolean hasCircularReference() {
		return this.hasCircularReference(new HashSet<Chunk>());
	}
	
	private boolean hasCircularReference(Set<Chunk> visitedChunks) {
		if(visitedChunks.contains(this))
			return true;
		visitedChunks.add(this);
		for(Chunk childChunk : this.getChunks()) {
			HashSet<Chunk> newVisitedChunks = new HashSet<Chunk>();
			newVisitedChunks.addAll(visitedChunks);
			boolean result = childChunk.hasCircularReference(newVisitedChunks);
			if(result)
				return true;
		}
		return false;
	}
}
