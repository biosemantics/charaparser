package semanticMarkup.eval;

import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * Evaluates the quality of a list of treatments given a correct list of treatments
 * @author rodenhausen
 */
public interface IEvaluator {

	/**
	 * @param createdTreatments
	 * @param correctTreatments
	 */
	public void evaluate(List<Treatment> createdTreatments, List<Treatment> correctTreatments);

	/**
	 * @return the description of this IEvaluator
	 */
	public String getDescription();
	
	/**
	 * @return the IEvaluationResult
	 */
	public IEvaluationResult getResult();
	
}
