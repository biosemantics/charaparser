package semanticMarkup.eval;

import semanticMarkup.eval.result.IEvaluationResult;
import semanticMarkup.markup.IMarkupResult;
import semanticMarkup.markup.IMarkupResultVisitor;

/**
 * Evaluates the quality of a list of treatments given a correct list of treatments
 * @author rodenhausen
 */
public interface IEvaluator extends IMarkupResultVisitor {

	/**
	 * @param createdTreatments
	 * @param correctTreatments
	 */
	public IEvaluationResult evaluate(IMarkupResult test, IMarkupResult correct);

	/**
	 * @return the description of this IEvaluator
	 */
	public String getDescription();
	
	
	/**
	 * @return the last computed result
	 */
	public IEvaluationResult getResult();	
}
