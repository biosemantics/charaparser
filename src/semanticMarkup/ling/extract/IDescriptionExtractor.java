package semanticMarkup.ling.extract;

import java.util.List;

import semanticMarkup.core.TreatmentElement;
import semanticMarkup.ling.chunk.ChunkCollector;

/**
 * An IDescriptionExtractor extracts a TreatmentElement from a list of treatmentChunkCollectors
 * @author rodenhausen
 */
public interface IDescriptionExtractor {

	/**
	 * @param treatmentChunkCollectors
	 * @return the treatmentElement constructred from the content of the treatmentChunkCollectors
	 */
	public TreatmentElement extract(List<ChunkCollector> treatmentChunkCollectors);

	/**
	 * @return a descriptive String of this IDescriptionExtractor
	 */
	public String getDescription();
	
}
