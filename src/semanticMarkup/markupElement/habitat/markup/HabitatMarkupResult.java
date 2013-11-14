package semanticMarkup.markupElement.habitat.markup;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupResult;
import semanticMarkup.markup.IMarkupResultVisitor;
import semanticMarkup.markupElement.habitat.model.HabitatsFileList;

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
