package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;

public interface IElevationWriter {

	void write(ElevationsFileList elevationsFileList,
			String outputDirectory) throws Exception;

}
