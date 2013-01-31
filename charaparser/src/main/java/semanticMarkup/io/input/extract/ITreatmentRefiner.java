package semanticMarkup.io.input.extract;

import semanticMarkup.core.Treatment;

public interface ITreatmentRefiner {

	public void refine(Treatment treatment, String clue, String nameForValues);
	
}
