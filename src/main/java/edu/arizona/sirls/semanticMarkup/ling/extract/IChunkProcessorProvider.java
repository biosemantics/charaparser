package edu.arizona.sirls.semanticMarkup.ling.extract;

import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;

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
