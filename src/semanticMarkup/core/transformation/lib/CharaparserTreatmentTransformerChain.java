package semanticMarkup.core.transformation.lib;

import semanticMarkup.core.transformation.TreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.description.DescriptionTreatmentTransformer;

import com.google.inject.Inject;

/**
 * A CharaparserTreatmentTransformerChain contains all the treatment transformation steps (ITreatmentTransformer) contained
 * in a previous version of Charaparser
 * @author rodenhausen
 */
public class CharaparserTreatmentTransformerChain extends TreatmentTransformerChain {

	/**
	 * @param habitatTreatmentTransformer
	 * @param dehyphenTreatmentTransformer
	 * @param markupDescriptionTreatmentTransformer
	 */
	@Inject
	public CharaparserTreatmentTransformerChain(HabitatTreatmentTransformer habitatTreatmentTransformer, 
			DehyphenTreatmentTransformer dehyphenTreatmentTransformer, DescriptionTreatmentTransformer markupDescriptionTreatmentTransformer) {
		treatmentTransformers.add(habitatTreatmentTransformer);
		treatmentTransformers.add(dehyphenTreatmentTransformer);
		treatmentTransformers.add(markupDescriptionTreatmentTransformer);
	}

}
