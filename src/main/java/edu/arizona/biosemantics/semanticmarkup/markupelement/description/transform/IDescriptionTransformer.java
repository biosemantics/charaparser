package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;


public interface IDescriptionTransformer {
	
	public TransformationReport transform(List<AbstractDescriptionsFile> descriptionsFiles);
	
}
