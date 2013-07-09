package semanticMarkup.markup;

import java.util.List;

public class MarkupCreatorChainResult implements IMarkupResult {

	private List<IMarkupResult> results;

	public MarkupCreatorChainResult(List<IMarkupResult> results) {
		this.results = results;
	}

	public List<IMarkupResult> getResults() {
		return results;
	}
	
	
}
