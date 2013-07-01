package semanticMarkup.eval.result;

import java.util.HashMap;
import java.util.Map;


public class PrecisionRecallEvaluationResult implements IEvaluationResult {

	private Map<String, PrecisionRecallResult> results = new HashMap<String, PrecisionRecallResult>();

	public PrecisionRecallResult put(String source, PrecisionRecallResult result) {
		return results.put(source, result);
	}

	public Map<String, PrecisionRecallResult> getResults() {
		return results;
	}

	public PrecisionRecallResult get(String source) {
		return results.get(source);
	}

}
