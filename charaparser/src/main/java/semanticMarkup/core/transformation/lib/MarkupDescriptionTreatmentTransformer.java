package semanticMarkup.core.transformation.lib;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.ITreatmentTransformer;

import com.google.inject.Inject;


public abstract class MarkupDescriptionTreatmentTransformer implements ITreatmentTransformer {

	@Inject
	public MarkupDescriptionTreatmentTransformer() {
	}


	public abstract List<Treatment> transform(List<Treatment> treatments);
}
