package edu.arizona.sirls.semanticMarkup.markupElement.description.transform;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFile;


public interface IDescriptionTransformer {
	
	public TransformationReport transform(List<AbstractDescriptionsFile> descriptionsFiles);
	
}
