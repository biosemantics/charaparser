package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupResult;

public interface IDescriptionMarkupResultReader {

	public DescriptionMarkupResult read(String testInputDirectory) throws Exception;

}
