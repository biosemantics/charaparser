package semanticMarkup.markupElement.description.ling.extract;

import java.util.List;

import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.markupElement.description.model.Description;

/**
 * An IDescriptionExtractor extracts a TreatmentElement from a list of treatmentChunkCollectors
 * @author rodenhausen
 */
public interface IDescriptionExtractor {

	/**
	 * @param description
	 * @param treatmentChunkCollectors
	 */
	public void extract(Description description, List<ChunkCollector> treatmentChunkCollectors);

	/**
	 * @return a descriptive String of this IDescriptionExtractor
	 */
	public String getDescription();
	
}
