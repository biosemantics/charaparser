package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Value;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;


public interface IHabitatTransformer {
	
	void transform(List<HabitatsFile> habitatsFiles);
	List<Value> parse(String text);

}
