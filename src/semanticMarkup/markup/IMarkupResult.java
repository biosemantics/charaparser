package semanticMarkup.markup;

public interface IMarkupResult {

	public void accept(IMarkupResultVisitor markupResultVisitor);
	
}
