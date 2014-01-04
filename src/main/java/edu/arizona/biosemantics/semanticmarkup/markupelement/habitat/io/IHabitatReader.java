package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.HabitatsFileList;

public interface IHabitatReader {

	public HabitatsFileList read(String inputDirectory) throws Exception;
	
}
