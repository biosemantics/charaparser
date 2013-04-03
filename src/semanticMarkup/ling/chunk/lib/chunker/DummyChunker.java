package semanticMarkup.ling.chunk.lib.chunker;

import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.IChunker;

/**
 * DummyChunker does not chunk. It contains a dummy implementation that can be passed in the configuration to have a placeholder IChunker.
 * @author rodenhausen
 */
public class DummyChunker implements IChunker {

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "dummy chunker";
	}
}
