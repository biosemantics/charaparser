package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFileList;

public interface IDistributionWriter {

	void write(DistributionsFileList distributionsFileList,
			String outputDirectory) throws Exception;

}
