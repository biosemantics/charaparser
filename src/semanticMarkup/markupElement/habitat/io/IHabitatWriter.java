package semanticMarkup.markupElement.habitat.io;

import semanticMarkup.markupElement.habitat.model.HabitatsFileList;

public interface IHabitatWriter {

	public void write(HabitatsFileList habitatsFileList, String inputDirectory) throws Exception;

}
