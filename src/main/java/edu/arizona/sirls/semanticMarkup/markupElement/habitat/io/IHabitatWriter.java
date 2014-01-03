package edu.arizona.sirls.semanticMarkup.markupElement.habitat.io;

import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;

public interface IHabitatWriter {

	public void write(HabitatsFileList habitatsFileList, String inputDirectory) throws Exception;

}
