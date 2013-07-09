package semanticMarkup.markupElement.habitat.io;

import semanticMarkup.markupElement.habitat.model.HabitatsFileList;;

public interface IHabitatReader {

	public HabitatsFileList read(String inputDirectory) throws Exception;
	
}
