package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;


public interface IHabitatTransformer {

	void transform(List<HabitatsFile> habitatsFiles);

}
