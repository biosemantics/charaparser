package semanticMarkup.eval;

public class StringEvaluationResult implements IEvaluationResult {

	private String result;
	
	public StringEvaluationResult(String result) {
		this.result = result;
	}
	
	@Override
	public String toString() {
		return result;
	}

}
