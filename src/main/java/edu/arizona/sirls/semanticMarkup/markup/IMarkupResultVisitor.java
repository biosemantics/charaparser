package edu.arizona.sirls.semanticMarkup.markup;

import edu.arizona.sirls.semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.markup.HabitatMarkupResult;

public interface IMarkupResultVisitor {
	
	public void visit(DescriptionMarkupResult descriptionMarkupResult);

	public void visit(HabitatMarkupResult habitatMarkupResult);
    
}
