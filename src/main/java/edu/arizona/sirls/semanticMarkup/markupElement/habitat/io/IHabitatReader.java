package edu.arizona.sirls.semanticMarkup.markupElement.habitat.io;

import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;

public interface IHabitatReader {

	public HabitatsFileList read(String inputDirectory) throws Exception;
	
}
