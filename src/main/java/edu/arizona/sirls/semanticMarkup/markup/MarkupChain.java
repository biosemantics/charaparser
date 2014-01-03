package edu.arizona.sirls.semanticMarkup.markup;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.sirls.semanticMarkup.markupElement.description.markup.DescriptionMarkupCreator;


public class MarkupChain implements IMarkupCreator {

	private List<IMarkupCreator> markupCreators = new LinkedList<IMarkupCreator>();
	
	public MarkupChain(DescriptionMarkupCreator charaparserMarkupCreator) {
		markupCreators.add(charaparserMarkupCreator);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}

	@Override
	public MarkupCreatorChainResult create() {
		List<IMarkupResult> results = new LinkedList<IMarkupResult>();
		for(IMarkupCreator markupCreator : markupCreators) {
			IMarkupResult markupResult = markupCreator.create();
			results.add(markupResult);
		}
		return  new MarkupCreatorChainResult(results);
	}
	
}
