package semanticMarkup.ling.chunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.log.LogLevel;

public class Chunk implements Cloneable {

	private ChunkType chunkType;
	private LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
	private HashMap<String, String> properties = new HashMap<String, String>(); 
	
	public Chunk(ChunkType chunkType) {
		this.chunkType = chunkType;
	}
	
	public int size() {
		return chunks.size();
	}
	
	public Chunk(ChunkType chunkType, Chunk chunk) {
		this.chunkType = chunkType;
		this.chunks.add(chunk);
	}
	
	public Chunk(ChunkType chunkType, Collection<Chunk> chunks) {
		this.chunkType = chunkType;
		for(Chunk chunk : chunks)
			this.chunks.add(chunk);
	}
	
	public ChunkType getChunkType() {
		return chunkType;
	}

	public void setChunkType(ChunkType chunkType) {
		this.chunkType = chunkType;
	}
	
	public void setChunks(LinkedHashSet<Chunk> chunks) {
		this.chunks = chunks;
	}

	public LinkedHashSet<Chunk> getChunks() {
		return this.chunks;
	}
	
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
	
	public boolean isUnassigned() {
		return this.isOfChunkType(ChunkType.UNASSIGNED);
	}
	
	public boolean isOfChunkType(ChunkType chunkType) {
		return this.chunkType.equals(chunkType);
	}
	
	public String getTerminalsText() {
		StringBuilder result = new StringBuilder();
		for(AbstractParseTree terminal : getTerminals()) 
			result.append(terminal.getTerminalsText()).append(" ");
		return result.toString().trim();
	}
	
	public boolean containsChildOfChunkType(ChunkType chunkType) {
		for(Chunk chunk : chunks) {
			if(chunk.getChunkType().equals(chunkType))
				return true;
		}
		return false;
	}
	
	public boolean containsChunkType(ChunkType chunkType) {
		if(this.chunkType.equals(chunkType))
			return true;
		for(Chunk chunk : chunks) 
			if(chunk.containsChunkType(chunkType)) {
				return true;
			}
		return false;
	}
	
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
	 * returns first chunk of this chunkType in depth first search manner
	 * @param chunkType
	 * @return
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
	 * returns first chunk of this chunkType in depth first search manner
	 * @param chunkType
	 * @return
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
	
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
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
	 * @return the number of child chunks left in this chunk so that it may be discarded when 0 is returned
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
	
	public void clearChunks() {
		this.chunks.clear();
	}
	
	public boolean contains(Chunk chunk) {
		if(this.equals(chunk))
			return true;
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
	
	public Chunk getMaxDepthChunkThatCoinsOnlyTerminal(AbstractParseTree terminal) {
		List<AbstractParseTree> terminals = this.getTerminals();
		if(terminals.size()==1 && terminals.contains(terminal)) {
			return this;
		} else if(terminals.contains(terminal)) {
			for(Chunk chunk : this.chunks) {
				Chunk maxChunk = chunk.getMaxDepthChunkThatCoinsOnlyTerminal(terminal);
				if(maxChunk!=null)
					return maxChunk;
			}
		}
		return null;
	}
	
	public Chunk getMaxDepthChunkThatCoinsAButNotB(Collection<AbstractParseTree> terminalsA, Collection<AbstractParseTree> terminalsB) {
		if(this.containsAll(terminalsA) && !this.containsAny(terminalsB)) {
			return this;
		} else {
			for(Chunk child : this.getChunks()) {
				Chunk result = child.getMaxDepthChunkThatCoinsAButNotB(terminalsA, terminalsB);
				if(result != null)
					return result;
			}
		}
		return null;
	}
	
	public boolean containsAll(Collection<AbstractParseTree> terminals) {
		boolean result = true;
		for(AbstractParseTree terminal : terminals) {
			result &= this.contains(terminal);
			if(!result)
				return result;
		}
		return result;
	}
	
	public boolean containsAny(Collection<AbstractParseTree> terminals) {
		boolean result = false;
		for(AbstractParseTree terminal : terminals) {
			result |= this.contains(terminal);
			if(result)
				return result;
		}
		return result;
	}
	
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
	
	public Chunk getCommonParent(Chunk chunkA, Chunk chunkB) {
		if(chunkA.contains(chunkB)) {
			return chunkA;
		}
		if(chunkB.contains(chunkA)) {
			return chunkB;
		}
		Chunk parentA = this.getParentChunk(chunkA);
		Chunk result = this.getCommonParent(parentA, chunkB);
		if(result != null)
			return result;
		
		Chunk parentB = this.getParentChunk(chunkB);
		result = this.getCommonParent(parentA, chunkB);
		if(result != null)
			return result;
		
		result = this.getCommonParent(parentA, parentB);
		return result;
	}
	
	public boolean containsAsChild(Chunk chunk) {
		return chunks.contains(chunk);
	}
	
	public void removeChunks(Collection<Chunk> chunks) {
		for(Chunk chunk : chunks)
			this.removeChunk(chunk);
	}
	
	public boolean isTerminal() {
		return this instanceof AbstractParseTree;
	}

	public List<Chunk> getChunksIncludingAfterTerminal(AbstractParseTree terminal) {
		List<Chunk> result = new LinkedList<Chunk>();
		for(Chunk chunk : this.getChunks()) {
			if(chunk.contains(terminal)) {
				result.addAll(chunk.getChunksIncludingAfterTerminal(terminal));
			} else if(chunk.equals(terminal)) {
				result.add(terminal);
			} else if(!result.isEmpty()) 
				result.add(chunk);
		}
		return result;
	}

	public Chunk getChunkOfTypeAndTerminal(ChunkType chunkType, AbstractParseTree ofTerminal) {
		Chunk result = null;
		if(this.getChunkType().equals(chunkType) && this.contains(ofTerminal)) {
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
	
	public Chunk getChildChunkOfTerminal(AbstractParseTree ofTerminal) {
		for(Chunk chunk : chunks) {
			if(chunk.contains(ofTerminal))
				return chunk;
		}
		return null;
	}

	public void addAndReplaceChunks(Chunk chunk) {
		AbstractParseTree firstTerminal = chunk.getTerminals().get(0);
		for(Chunk child : chunks) {
			if(child.contains(firstTerminal)) {
				child.addAndReplaceChunks(chunk);
				break;
			}
			if(child.equals(firstTerminal)) {
				LinkedHashSet<Chunk> newChunks = new LinkedHashSet<Chunk>();
				for(Chunk childChunk : chunks) {
					if(chunk.contains(childChunk))
						newChunks.add(chunk);
					else 
						newChunks.add(childChunk);
				}
				chunks = newChunks;
				break;
			}
		}
	}
	
	public List<Chunk> getChunksWithoutTerminal(AbstractParseTree terminal) {
		List<Chunk> result = new LinkedList<Chunk>();
		for(Chunk chunk : this.getChunks()) {
			if(chunk.contains(terminal) || chunk.equals(terminal)) {
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
			log(LogLevel.ERROR, e);
		}
		return null;
	}

	public void replaceChunk(Chunk toReplaceChunk, Chunk replaceChunk) {
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
				break;
			}
			else
				chunk.replaceChunk(toReplaceChunk, replaceChunk);
		}
	}
}
