package edu.arizona.biosemantics.semanticmarkup.markup;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup.DistributionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.EcologyMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup.ElevationMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.markup.HabitatMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup.PhenologyMarkupResult;

public interface IMarkupResultVisitor {
	
	public void visit(DescriptionMarkupResult descriptionMarkupResult);

	public void visit(HabitatMarkupResult habitatMarkupResult);
	
	public void visit(edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup.HabitatMarkupResult habitatMarkupResult);
	
	public void visit(DistributionMarkupResult descriptionMarkupResult);
	
	public void visit(ElevationMarkupResult descriptionMarkupResult);
	
	public void visit(PhenologyMarkupResult descriptionMarkupResult);

	public void visit(EcologyMarkupResult ecologyMarkupResult);
    
}

