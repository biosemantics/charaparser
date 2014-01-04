package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;


/**
 * An IDescriptionExtractor extracts a TreatmentElement from a list of treatmentChunkCollectors
 * @author rodenhausen
 */
public interface IDescriptionExtractor {

	/**
	 * @param description
	 * @param descriptionNumber 
	 * @param treatmentChunkCollectors
	 */
	public void extract(Description description, int descriptionNumber, List<ChunkCollector> treatmentChunkCollectors);

	/**
	 * @return a descriptive String of this IDescriptionExtractor
	 */
	public String getDescription();
	
}
