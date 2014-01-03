package edu.arizona.sirls.semanticMarkup.eval.result;


/**
 * A String representation of a IEvaluationResult
 * @author rodenhausen
 *
 */
public class StringEvaluationResult implements IEvaluationResult {

	private String result;
	
	/**
	 * @param result
	 */
	public StringEvaluationResult(String result) {
		this.result = result;
	}
	
	/**
	 * @return the String result
	 */
	public String getResult() {
		return this.result;
	}
}
