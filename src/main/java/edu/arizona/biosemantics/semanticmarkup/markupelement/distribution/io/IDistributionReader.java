package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFileList;

public interface IDistributionReader {

	DistributionsFileList read(String inputDirectory) throws Exception;

}
