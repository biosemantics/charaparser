package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.markup;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.HabitatsFileList;

public class HabitatMarkupResult implements IMarkupResult {

	private HabitatsFileList habitatsFileList;

	public HabitatMarkupResult(HabitatsFileList habitatsFileList) {
		this.habitatsFileList = habitatsFileList;
	}

	public HabitatsFileList getResult() {
		return habitatsFileList;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}
	
}
