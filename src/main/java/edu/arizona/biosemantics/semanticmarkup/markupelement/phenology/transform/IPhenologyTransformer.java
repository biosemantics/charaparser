package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;


public interface IPhenologyTransformer {

	void transform(List<PhenologiesFile> phenologiesFiles);

}
