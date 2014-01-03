package edu.arizona.sirls.semanticMarkup.markupElement.description.io;

import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFileList;

public interface IDescriptionReader {

	public DescriptionsFileList read(String inputDirectory) throws Exception;

	
}
