package semanticMarkup.core.transformation.lib.description;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.ITreatmentTransformer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment 
 * @author rodenhausen
 */
public abstract class DescriptionTreatmentTransformer implements ITreatmentTransformer {

	protected boolean parallelProcessing;
	protected String version;

	/**
	 * @param parallelProcessing to use or not
	 */
	@Inject
	public DescriptionTreatmentTransformer(@Named("Version")String version, 
			@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing) {
		this.version = version;
		this.parallelProcessing = parallelProcessing;
	}

	public abstract List<Treatment> transform(List<Treatment> treatments);
}
