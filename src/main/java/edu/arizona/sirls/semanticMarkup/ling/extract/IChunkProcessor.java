package edu.arizona.sirls.semanticMarkup.ling.extract;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import edu.arizona.sirls.semanticMarkup.model.Element;


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
