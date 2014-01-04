package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.HabitatsFileList;

public interface IHabitatWriter {

	public void write(HabitatsFileList habitatsFileList, String inputDirectory) throws Exception;

}
