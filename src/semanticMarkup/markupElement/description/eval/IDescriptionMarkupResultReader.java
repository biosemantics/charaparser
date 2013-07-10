package semanticMarkup.markupElement.description.eval;

import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;

public interface IDescriptionMarkupResultReader {

	public DescriptionMarkupResult read(String testInputDirectory) throws Exception;

}
