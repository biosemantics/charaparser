package semanticMarkup.ling.extract;

import java.util.List;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.ling.chunk.Chunk;

public interface IChunkProcessor {

	public List<DescriptionTreatmentElement> process(Chunk chunk, ProcessingContext processingContext);

	public String getDescription();
	
}
