package semanticMarkup.ling.extract;

import java.util.List;

import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.model.Element;

/**
 * IChunkProcessor processes a chunk in a processing context
 * @author rodenhausen
 */
public interface IChunkProcessor {

	/**
	 * @param chunk
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from the processing of chunk in processingContext
	 */
	public List<? extends Element> process(Chunk chunk, ProcessingContext processingContext);

	/**
	 * @return descriptive String of this IChunkProcessor
	 */
	public String getDescription();
	
}
