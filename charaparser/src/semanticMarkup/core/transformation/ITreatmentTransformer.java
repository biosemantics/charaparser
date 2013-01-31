package semanticMarkup.core.transformation;

import java.util.List;

import semanticMarkup.core.Treatment;


public interface ITreatmentTransformer {

	public List<Treatment> transform(List<Treatment> treatments);
	
}
