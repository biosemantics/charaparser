package semanticMarkup.eval.result;

public class PrecisionRecallResult {

	private double precision = -1.0;
	private double recall = -1.0;

	public PrecisionRecallResult(double precision,
			double recall) {
		super();
		this.precision = precision;
		this.recall = recall;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	
	@Override
	public String toString() {
		return "Precision: " + precision + " Recall: " + recall;
	}

}
