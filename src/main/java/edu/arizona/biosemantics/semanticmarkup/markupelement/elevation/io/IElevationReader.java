package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;

public interface IElevationReader {

	ElevationsFileList read(String inputDirectory) throws Exception;

}
