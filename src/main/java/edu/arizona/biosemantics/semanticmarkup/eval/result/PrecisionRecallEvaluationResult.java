package edu.arizona.biosemantics.semanticmarkup.eval.result;

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
		
		//if precision or recall = NaN is returned there was a division by 0 (due to the fact that no correct results existed at all for recall
		// or no retrieved results existed at all for precision)
		int precisionValues = 0;
		int recallValues = 0;
		
		for(String source : results.keySet()) {
			stringBuilder.append(source + ": " + results.get(source).toString() + "\n");
			double precision = results.get(source).getPrecision();
			double recall = results.get(source).getRecall();
			
			if (!Double.isNaN(precision)) {
				averagePrecision += precision;
				precisionValues++;
			}
			if (!Double.isNaN(recall)) {
				averageRecall += recall;
				recallValues++;
			}
		}
		averagePrecision = averagePrecision / precisionValues;
		averageRecall = averageRecall / recallValues;
		stringBuilder.append("Average Precision: " + averagePrecision + " Average Recall: " + averageRecall);
		return stringBuilder.toString();
	}
}
