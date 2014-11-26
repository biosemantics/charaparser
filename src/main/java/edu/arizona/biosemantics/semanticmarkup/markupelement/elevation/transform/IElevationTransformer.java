/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

import java.util.LinkedHashSet;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;

/**
 * @author Hong Cui
 *
 */
public interface IElevationTransformer {
	void transform(List<ElevationsFile> elevationsFiles);
	LinkedHashSet<Character> parse(String text);
}
