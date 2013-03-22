package semanticMarkup.core.transformation.lib;

import semanticMarkup.core.transformation.TreatmentTransformerChain;

import com.google.inject.Inject;


public class CharaparserTreatmentTransformerChain extends TreatmentTransformerChain {

	@Inject
	public CharaparserTreatmentTransformerChain(HabitatTreatmentTransformer habitatTreatmentTransformer, 
			DehyphenTreatmentTransformer dehyphenTreatmentTransformer, MarkupDescriptionTreatmentTransformer markupDescriptionTreatmentTransformer) {
		treatmentTransformers.add(habitatTreatmentTransformer);
		treatmentTransformers.add(dehyphenTreatmentTransformer);
		treatmentTransformers.add(markupDescriptionTreatmentTransformer);
	}

}
