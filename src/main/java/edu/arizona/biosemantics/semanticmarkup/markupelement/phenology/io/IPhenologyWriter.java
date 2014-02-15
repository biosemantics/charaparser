package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;

public interface IPhenologyWriter {

	void write(PhenologiesFileList phenologiesFileList,
			String outputDirectory) throws Exception;

}
