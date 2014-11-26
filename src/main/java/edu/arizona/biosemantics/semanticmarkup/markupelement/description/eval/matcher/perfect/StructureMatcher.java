package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.perfect;

import edu.arizona.biosemantics.semanticmarkup.eval.matcher.AbstractMatcher;
import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;


public class StructureMatcher extends AbstractMatcher implements IMatcher<BiologicalEntity> {

	@Override
	public boolean isMatch(BiologicalEntity structureA, BiologicalEntity structureB) {
		boolean result = this.equalsOrNull("structure name", structureA.getName(), structureB.getName())  &&
				this.equalsOrNull("structure constraint", structureA.getConstraint(), structureB.getConstraint());
		return result;
	}
}
