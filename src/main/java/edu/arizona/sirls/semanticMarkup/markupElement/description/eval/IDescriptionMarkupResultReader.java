package edu.arizona.sirls.semanticMarkup.markupElement.description.eval;

import edu.arizona.sirls.semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;

public interface IDescriptionMarkupResultReader {

	public DescriptionMarkupResult read(String testInputDirectory) throws Exception;

}
