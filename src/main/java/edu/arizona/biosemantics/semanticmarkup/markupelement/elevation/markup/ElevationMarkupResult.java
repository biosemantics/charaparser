package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;

//TODO: If elevation markup is to be evaluated
public class ElevationMarkupResult implements IMarkupResult {

	public ElevationMarkupResult(List<Elevation> elevations) {
		
	}
	
	public ElevationMarkupResult(ElevationsFileList elevationsFileList) {
		
	}

	public List<Elevation> getResult() {
		return null;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}

}
