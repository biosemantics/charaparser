package semanticMarkup.ling.chunk;

public interface IChunker {

	public void chunk(ChunkCollector chunkCollector);

	public String getName();
	
}
