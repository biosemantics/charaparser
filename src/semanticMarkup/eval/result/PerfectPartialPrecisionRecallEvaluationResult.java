package semanticMarkup.eval.result;

import java.util.HashMap;
import java.util.Map;


public class PerfectPartialPrecisionRecallEvaluationResult implements IEvaluationResult {

	private Map<String, PrecisionRecallEvaluationResult> results = new HashMap<String, PrecisionRecallEvaluationResult>();

	public PrecisionRecallEvaluationResult put(String name, PrecisionRecallEvaluationResult result) {
		return results.put(name, result);
	}

	public Map<String, PrecisionRecallEvaluationResult> getResults() {
		return results;
	}

	public PrecisionRecallEvaluationResult get(String name) {
		return results.get(name);
	}

}
