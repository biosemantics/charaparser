package semanticMarkup.ling.extract;

import semanticMarkup.ling.chunk.ChunkType;

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
