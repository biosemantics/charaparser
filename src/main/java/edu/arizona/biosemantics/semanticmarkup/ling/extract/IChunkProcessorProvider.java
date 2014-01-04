package edu.arizona.biosemantics.semanticmarkup.ling.extract;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;

/**
 * IChunkProcessorProvider provides its clients with IChunkProcessors
 * @author rodenhausen
 */
public interface IChunkProcessorProvider {

	/**
	 * @param chunkType
	 * @return a IChunkProcessor for the chunkType
	 */
	public IChunkProcessor getChunkProcessor(ChunkType chunkType);
	
}
