package semanticMarkup.ling.extract;

import java.util.List;

import semanticMarkup.core.TreatmentElement;
import semanticMarkup.ling.chunk.ChunkCollector;

public interface IDescriptionExtractor {

	public TreatmentElement extract(List<ChunkCollector> treatmentChunkCollectors);

	public String getDescription();
	
}
