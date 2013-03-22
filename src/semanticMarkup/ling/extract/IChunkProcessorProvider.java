package semanticMarkup.ling.extract;

import semanticMarkup.ling.chunk.ChunkType;

public interface IChunkProcessorProvider {

	public IChunkProcessor getChunkProcessor(ChunkType chunkType);
	
}
