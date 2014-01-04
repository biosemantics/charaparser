package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.partial;

import edu.arizona.biosemantics.semanticmarkup.eval.matcher.AbstractMatcher;
import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;

public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.valuesNullOrContainedEitherWay("name", structureA.getName(), structureB.getName());
		return result;
	}
}
