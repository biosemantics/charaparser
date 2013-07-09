package semanticMarkup.markupElement.description.markup;

import semanticMarkup.markup.IMarkupResult;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

public class DescriptionMarkupResult implements IMarkupResult {

	private DescriptionsFileList descriptionsFileList;

	public DescriptionMarkupResult(DescriptionsFileList descriptionReferences) {
		this.descriptionsFileList = descriptionReferences;
	}

	public DescriptionsFileList getResult() {
		return descriptionsFileList;
	}

}
