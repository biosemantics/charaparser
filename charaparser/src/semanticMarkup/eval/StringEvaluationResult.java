package semanticMarkup.eval;

public class StringEvaluationResult implements IEvaluationResult {

	private String result;
	
	public StringEvaluationResult(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return this.result;
	}
}
