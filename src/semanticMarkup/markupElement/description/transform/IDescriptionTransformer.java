package semanticMarkup.markupElement.description.transform;

import java.util.List;

import semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFile;

public interface IDescriptionTransformer {
	
	public TransformationReport transform(List<AbstractDescriptionsFile> descriptionsFiles);
	
}
