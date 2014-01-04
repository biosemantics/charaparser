package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment 
 * @author rodenhausen
 */
public abstract class AbstractDescriptionTransformer implements IDescriptionTransformer {

	protected boolean parallelProcessing;
	protected String version;

	/**
	 * @param parallelProcessing to use or not
	 */
	@Inject
	public AbstractDescriptionTransformer(@Named("Version")String version,
			@Named("DescriptionTransformer_parallelProcessing")boolean parallelProcessing) {
		this.parallelProcessing = parallelProcessing;
		this.version = version;
	}
}
