package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;

public interface IHabitatReader {

	HabitatsFileList read(String inputDirectory) throws Exception;

}

