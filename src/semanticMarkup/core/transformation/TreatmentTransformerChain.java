package semanticMarkup.core.transformation;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.core.Treatment;


public class TreatmentTransformerChain {
	
	protected List<ITreatmentTransformer> treatmentTransformers = new LinkedList<ITreatmentTransformer>();
	
	public List<Treatment> transform(List<Treatment> treatments) {
		for(ITreatmentTransformer treatmentTransformer : treatmentTransformers) {
			treatments = treatmentTransformer.transform(treatments);
		}
		return treatments;
	}

	public boolean add(ITreatmentTransformer treatmentTransformer) {
		return treatmentTransformers.add(treatmentTransformer);
	}

	public boolean remove(ITreatmentTransformer treatmentTransformer) {
		return treatmentTransformers.remove(treatmentTransformer);
	}
	
	
}
