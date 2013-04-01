package semanticMarkup.core.transformation;

import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * ITreatmentTransformer transforms a list of treatments in a certain way into a output list of treatments
 * @author rodenhausen
 */
public interface ITreatmentTransformer {

	public List<Treatment> transform(List<Treatment> treatments);
	
}
