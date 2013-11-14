package semanticMarkup.markupElement.description.io;

import semanticMarkup.markupElement.description.model.DescriptionsFileList;

public interface IDescriptionReader {

	public DescriptionsFileList read(String inputDirectory) throws Exception;

	
}
