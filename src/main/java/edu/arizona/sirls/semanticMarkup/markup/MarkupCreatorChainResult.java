package edu.arizona.sirls.semanticMarkup.markup;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;


public class MarkupCreatorChainResult implements IMarkupResult {

	private List<IMarkupResult> results;

	public MarkupCreatorChainResult(List<IMarkupResult> results) {
		this.results = results;
	}

	public List<IMarkupResult> getResults() {
		return results;
	}


	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		
	}
}
