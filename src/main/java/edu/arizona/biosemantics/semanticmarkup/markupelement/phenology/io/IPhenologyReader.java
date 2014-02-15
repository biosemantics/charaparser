package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;

public interface IPhenologyReader {

	PhenologiesFileList read(String inputDirectory) throws Exception;

}
