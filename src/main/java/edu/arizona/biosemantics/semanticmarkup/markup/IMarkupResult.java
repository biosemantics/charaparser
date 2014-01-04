package edu.arizona.biosemantics.semanticmarkup.markup;

public interface IMarkupResult {

	public void accept(IMarkupResultVisitor markupResultVisitor);
	
}
