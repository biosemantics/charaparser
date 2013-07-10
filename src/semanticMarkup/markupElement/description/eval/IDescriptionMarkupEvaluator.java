package semanticMarkup.markupElement.description.eval;

import semanticMarkup.eval.IEvaluationResult;
import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;

/**
 * Evaluates the quality of a list of treatments given a correct list of treatments
 * @author rodenhausen
 */
public interface IDescriptionMarkupEvaluator extends IEvaluator {

	/**
	 * @param result
	 * @param correctTreatments
	 */
	public void evaluate(DescriptionMarkupResult descriptionMarkupResult, DescriptionMarkupResult correctDescriptionMarkupResult);

	/**
	 * @return the description of this IEvaluator
	 */
	public String getDescription();
	
	/**
	 * @return the IEvaluationResult
	 */
	public IEvaluationResult getResult();
	
}
