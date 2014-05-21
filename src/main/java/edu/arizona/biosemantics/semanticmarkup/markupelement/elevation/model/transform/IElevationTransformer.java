package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;

public interface IElevationTransformer {

	void transform(List<ElevationsFile> elevationsFiles);

}

