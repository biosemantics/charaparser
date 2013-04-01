package semanticMarkup.core.transformation;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * A TreatmentTransformerChain transforms a list of treatments consecutively using ITreatmentTransformers
 * @author rodenhausen
 */
public class TreatmentTransformerChain {
	
	protected List<ITreatmentTransformer> treatmentTransformers = new LinkedList<ITreatmentTransformer>();
	
	/**
	 * @param treatments
	 * @return the transformed treatments
	 */
	public List<Treatment> transform(List<Treatment> treatments) {
		for(ITreatmentTransformer treatmentTransformer : treatmentTransformers) {
			treatments = treatmentTransformer.transform(treatments);
		}
		return treatments;
	}

	/**
	 * @param treatmentTransformer
	 * @return if the treatmentTransformer has been added successfully
	 */
	public boolean add(ITreatmentTransformer treatmentTransformer) {
		return treatmentTransformers.add(treatmentTransformer);
	}

	/**
	 * @param treatmentTransformer
	 * @return if the treatmentsTransformer has been removed successfully
	 */
	public boolean remove(ITreatmentTransformer treatmentTransformer) {
		return treatmentTransformers.remove(treatmentTransformer);
	}
	
	
}
