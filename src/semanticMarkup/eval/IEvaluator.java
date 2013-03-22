package semanticMarkup.eval;

import java.util.List;

import semanticMarkup.core.Treatment;

public interface IEvaluator {

	public void evaluate(List<Treatment> createdTreatments, List<Treatment> correctTreatments);

	public String getDescription();
	
	public IEvaluationResult getResult();
	
}
