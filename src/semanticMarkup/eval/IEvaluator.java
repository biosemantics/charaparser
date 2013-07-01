package semanticMarkup.eval;

import semanticMarkup.eval.result.IEvaluationResult;

/**
 * Evaluates the quality of a list of treatments given a correct list of treatments
 * @author rodenhausen
 */
public interface IEvaluator {

	/**
	 * @param createdTreatments
	 * @param correctTreatments
	 */
	public IEvaluationResult evaluate(String testDirectoryPath, String correctDirectoryPath);

	/**
	 * @return the description of this IEvaluator
	 */
	public String getDescription();
	
}
