package semanticMarkup.core.transformation.lib;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.ITreatmentTransformer;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public abstract class MarkupDescriptionTreatmentTransformer implements ITreatmentTransformer {

	protected boolean parallelProcessing;


	@Inject
	public MarkupDescriptionTreatmentTransformer(@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing) {
		this.parallelProcessing = parallelProcessing;
	}


	public abstract List<Treatment> transform(List<Treatment> treatments);
}
