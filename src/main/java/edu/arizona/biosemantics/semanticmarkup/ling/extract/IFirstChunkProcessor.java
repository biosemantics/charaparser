package edu.arizona.biosemantics.semanticmarkup.ling.extract;

/**
 * An IFirstChunkProcessor is used for the first chunk appearing in a sentence
 * @author rodenhausen
 */
public interface IFirstChunkProcessor extends IChunkProcessor {

	/**
	 * @return if the first chunk ought to be skipped consequently (has sufficiently been treated by this IChunkProcessor)
	 */
	public int skipFirstNChunk();

}
