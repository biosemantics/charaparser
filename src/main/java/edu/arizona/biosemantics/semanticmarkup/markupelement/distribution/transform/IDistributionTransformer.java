package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.LinkedHashSet;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;

public interface IDistributionTransformer {

	void transform(List<DistributionsFile> distributionsFiles);

}

