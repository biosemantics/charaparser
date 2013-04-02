package semanticMarkup.io.input.extract;

import semanticMarkup.core.Treatment;

/**
 * ITreatmentRefiner refines a treatments creating new descriptive treatment elements implicitly available in the data given 
 * TODO: Should probably be moved somewhere else, e.g. ITreatmentRefiners as ITreatmentTransformer
 * @author rodenhausen
 */
public interface ITreatmentRefiner {

	/**
	 * @param treatment
	 * @param clue
	 * @param nameForValues
	 */
	public void refine(Treatment treatment, String clue, String nameForValues);
	
}
