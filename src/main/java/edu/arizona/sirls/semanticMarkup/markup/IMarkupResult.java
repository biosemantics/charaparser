package edu.arizona.sirls.semanticMarkup.markup;

public interface IMarkupResult {

	public void accept(IMarkupResultVisitor markupResultVisitor);
	
}
