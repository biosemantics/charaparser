package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;

public interface IDistributionTransformer {

	void transform(List<DistributionsFile> distributionsFiles);

}

