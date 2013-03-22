package semanticMarkup.ling.extract;

import java.util.List;

import semanticMarkup.core.description.DescriptionTreatmentElement;

public interface ILastChunkProcessor {

	List<DescriptionTreatmentElement> process(
			ProcessingContext processingContext);

}
