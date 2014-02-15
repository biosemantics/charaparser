package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;

public interface IHabitatWriter {

	void write(HabitatsFileList habitatsFileList,
			String outputDirectory) throws Exception;

}
