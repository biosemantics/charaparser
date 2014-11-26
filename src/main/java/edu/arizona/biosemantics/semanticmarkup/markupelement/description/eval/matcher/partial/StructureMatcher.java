package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.partial;

import edu.arizona.biosemantics.semanticmarkup.eval.matcher.AbstractMatcher;
import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;

public class StructureMatcher extends AbstractMatcher implements IMatcher<BiologicalEntity> {

	@Override
	public boolean isMatch(BiologicalEntity structureA, BiologicalEntity structureB) {
		boolean result = this.valuesNullOrContainedEitherWay("name", structureA.getName(), structureB.getName());
		return result;
	}
}
