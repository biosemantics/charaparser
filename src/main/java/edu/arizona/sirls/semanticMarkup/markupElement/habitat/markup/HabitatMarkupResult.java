package edu.arizona.sirls.semanticMarkup.markupElement.habitat.markup;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markup.IMarkupResult;
import edu.arizona.sirls.semanticMarkup.markup.IMarkupResultVisitor;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;

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
