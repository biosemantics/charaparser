package semanticMarkup.markupElement.habitat.markup;

import semanticMarkup.markup.IMarkupResult;
import semanticMarkup.markupElement.habitat.model.HabitatsFileList;

public class HabitatMarkupResult implements IMarkupResult {

	private HabitatsFileList habitatsFileList;

	public HabitatMarkupResult(HabitatsFileList habitatsFileList) {
		this.habitatsFileList = habitatsFileList;
	}

	public HabitatsFileList getResult() {
		return habitatsFileList;
	}
	
}
