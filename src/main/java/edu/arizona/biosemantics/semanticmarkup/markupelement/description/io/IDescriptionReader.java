package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;

public interface IDescriptionReader {

	public DescriptionsFileList read(String inputDirectory) throws Exception;

	
}
