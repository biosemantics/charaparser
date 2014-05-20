package edu.arizona.biosemantics.semanticmarkup.ling.extract;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;

/**
 * An IFirstChunkProcessor is used for the first chunk appearing in a sentence
 * @author rodenhausen
 */
public interface IFirstChunkProcessor extends IChunkProcessor {

	/**
	 * @return if the first chunk ought to be skipped consequently (has sufficiently been treated by this IChunkProcessor)
	 */
	public int skipFirstNChunk();

	public int skipHeading(List<Chunk> chunks);
	
	public void setFirstSentence();
	
	public void unsetFirstSentence();

}
