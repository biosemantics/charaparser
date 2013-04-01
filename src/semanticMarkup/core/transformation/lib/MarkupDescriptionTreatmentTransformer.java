package semanticMarkup.core.transformation.lib;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.ITreatmentTransformer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment 
 * @author rodenhausen
 */
public abstract class MarkupDescriptionTreatmentTransformer implements ITreatmentTransformer {

	protected boolean parallelProcessing;

	/**
	 * @param parallelProcessing to use or not
	 */
	@Inject
	public MarkupDescriptionTreatmentTransformer(@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing) {
		this.parallelProcessing = parallelProcessing;
	}

	public abstract List<Treatment> transform(List<Treatment> treatments);
}
