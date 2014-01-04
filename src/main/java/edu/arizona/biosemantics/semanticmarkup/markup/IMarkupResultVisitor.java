package edu.arizona.biosemantics.semanticmarkup.markup;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.markup.HabitatMarkupResult;

public interface IMarkupResultVisitor {
	
	public void visit(DescriptionMarkupResult descriptionMarkupResult);

	public void visit(HabitatMarkupResult habitatMarkupResult);
    
}
