package semanticMarkup.markup;

import java.util.List;

import semanticMarkup.log.LogLevel;

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
