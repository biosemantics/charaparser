package edu.arizona.sirls.semanticMarkup.ling.chunk;

import java.util.Iterator;

import edu.arizona.sirls.semanticMarkup.ling.parse.AbstractParseTree;


/**
 * ChunkCollectorIterator allows to iterate over the Chunks of a chunkCollector
 * @author rodenhausen
 * @param <T>
 */
public class ChunkCollectorIterator<T> implements Iterator<Chunk> {

	private ChunkCollector chunkCollector;
	private Iterator<AbstractParseTree> terminalsIterator;

	/**
	 * @param chunkCollector
	 */
	public ChunkCollectorIterator(ChunkCollector chunkCollector) {
		this.chunkCollector = chunkCollector;
		this.terminalsIterator = chunkCollector.getTerminals().iterator();
	}

	@Override
	public boolean hasNext() {
		return terminalsIterator.hasNext();
	}

	@Override
	public Chunk next() {
		return chunkCollector.getChunk(terminalsIterator.next());
	}

	@Override
	public void remove() {
		//remove is not supported
	}

}
