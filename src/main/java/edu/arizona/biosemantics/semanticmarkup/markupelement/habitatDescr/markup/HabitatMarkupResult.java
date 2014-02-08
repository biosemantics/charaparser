package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;

//TODO: If habitat markup is to be evaluated
public class HabitatMarkupResult implements IMarkupResult {

	public HabitatMarkupResult(List<Habitat> habitats) {
		
	}
	
	public HabitatMarkupResult(HabitatsFileList habitatsFileList) {
		
	}

	public List<Habitat> getResult() {
		return null;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}

}
