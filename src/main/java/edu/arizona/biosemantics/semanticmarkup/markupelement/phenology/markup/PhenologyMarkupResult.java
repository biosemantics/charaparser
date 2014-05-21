package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;

//TODO: If phenology markup is to be evaluated
public class PhenologyMarkupResult implements IMarkupResult {

	public PhenologyMarkupResult(List<Phenology> phenologies) {
		
	}
	
	public PhenologyMarkupResult(PhenologiesFileList phenologiesFileList) {
		
	}

	public List<Phenology> getResult() {
		return null;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}

}

