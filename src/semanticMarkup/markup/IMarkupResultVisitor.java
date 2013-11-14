package semanticMarkup.markup;

import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;
import semanticMarkup.markupElement.habitat.markup.HabitatMarkupResult;

public interface IMarkupResultVisitor {
	
	public void visit(DescriptionMarkupResult descriptionMarkupResult);

	public void visit(HabitatMarkupResult habitatMarkupResult);
    
}
