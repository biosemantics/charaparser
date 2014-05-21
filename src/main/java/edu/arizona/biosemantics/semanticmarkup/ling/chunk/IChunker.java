package edu.arizona.biosemantics.semanticmarkup.ling.chunk;

/**
 * IChunker refines the chunking result stored in a ChunkCollector in a bottom-up fashion
 * @author rodenhausen
 */
public interface IChunker {

	/**
	 * Further chunk stored result in chunkCollector
	 * @param chunkCollector
	 */
	public void chunk(ChunkCollector chunkCollector);

	/**
	 * @return descriptive name of IChunker
	 */
	public String getName();
	
}
