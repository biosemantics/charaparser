package semanticMarkup.eval.result;

import java.util.LinkedHashMap;
import java.util.Map;


public class PerfectPartialPrecisionRecallEvaluationResult implements IEvaluationResult {

	private Map<String, PrecisionRecallEvaluationResult> results = new LinkedHashMap<String, PrecisionRecallEvaluationResult>();

	public PrecisionRecallEvaluationResult put(String name, PrecisionRecallEvaluationResult result) {
		return results.put(name, result);
	}

	public Map<String, PrecisionRecallEvaluationResult> getResults() {
		return results;
	}

	public PrecisionRecallEvaluationResult get(String name) {
		return results.get(name);
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for(String name : results.keySet()) {
			stringBuilder.append(name + ":\n" + results.get(name).toString() + "\n");
			stringBuilder.append("---");
		}
		return stringBuilder.toString();
	}

}
