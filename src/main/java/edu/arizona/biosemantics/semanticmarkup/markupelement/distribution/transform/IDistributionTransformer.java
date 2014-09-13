package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Value;

public interface IDistributionTransformer {

	void transform(List<DistributionsFile> distributionsFiles);
	List<Value> parse(String text);

}

