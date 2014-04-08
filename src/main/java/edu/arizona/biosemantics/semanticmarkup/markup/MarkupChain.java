package edu.arizona.biosemantics.semanticmarkup.markup;

import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.IDescriptionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup.IDistributionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.IEcologyMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup.IElevationMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup.IHabitatMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup.IPhenologyMarkupCreator;


public class MarkupChain implements IMarkupCreator {

	private List<IMarkupCreator> markupCreators = new LinkedList<IMarkupCreator>();
	
	@Inject
	public MarkupChain(IDescriptionMarkupCreator descriptionMarkupCreator, IHabitatMarkupCreator habitatMarkupCreator, 
			IPhenologyMarkupCreator phenlogyMarkupCreator, IElevationMarkupCreator elevationMarkupCreator, 
			IDistributionMarkupCreator distributionMarkupCreator, IEcologyMarkupCreator ecologyMarkupCreator) {
		if(descriptionMarkupCreator != null)
			markupCreators.add(descriptionMarkupCreator);
		if(habitatMarkupCreator != null)
			markupCreators.add(habitatMarkupCreator);
		if(phenlogyMarkupCreator != null)
			markupCreators.add(phenlogyMarkupCreator);
		if(elevationMarkupCreator != null)
			markupCreators.add(elevationMarkupCreator);
		if(distributionMarkupCreator != null)
			markupCreators.add(distributionMarkupCreator);
		if(ecologyMarkupCreator != null)
			markupCreators.add(ecologyMarkupCreator);
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
