package semanticMarkup.eval.result;

import java.util.LinkedHashMap;
import java.util.Map;


public class PrecisionRecallEvaluationResult implements IEvaluationResult {

	private Map<String, PrecisionRecallResult> results = new LinkedHashMap<String, PrecisionRecallResult>();

	public PrecisionRecallResult put(String source, PrecisionRecallResult result) {
		return results.put(source, result);
	}

	public Map<String, PrecisionRecallResult> getResults() {
		return results;
	}

	public PrecisionRecallResult get(String source) {
		return results.get(source);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		double averagePrecision = 0.0;
		double averageRecall = 0.0;
		for(String source : results.keySet()) {
			stringBuilder.append(source + ": " + results.get(source).toString() + "\n");
			averagePrecision += results.get(source).getPrecision();
			averageRecall += results.get(source).getRecall();
		}
		averagePrecision = averagePrecision / results.size();
		averageRecall = averageRecall / results.size();
		stringBuilder.append("Average Precision: " + averagePrecision + " Average Recall: " + averageRecall);
		return stringBuilder.toString();
	}
}
