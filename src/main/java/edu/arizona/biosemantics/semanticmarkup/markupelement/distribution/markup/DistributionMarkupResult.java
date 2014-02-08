package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFileList;

//TODO: If distribution markup is to be evaluated
public class DistributionMarkupResult implements IMarkupResult {

	public DistributionMarkupResult(List<Distribution> distributions) {
		
	}
	
	public DistributionMarkupResult(DistributionsFileList distributionsFileList) {
		
	}

	public List<Distribution> getResult() {
		return null;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}

}
