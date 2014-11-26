package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.LinkedHashSet;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;

public interface IPhenologyTransformer {

	void transform(List<PhenologiesFile> phenologiesFiles);
	LinkedHashSet<Character> parse(String text);
}
