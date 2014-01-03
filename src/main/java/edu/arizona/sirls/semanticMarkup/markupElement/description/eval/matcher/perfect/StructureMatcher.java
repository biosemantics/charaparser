package edu.arizona.sirls.semanticMarkup.markupElement.description.eval.matcher.perfect;

import edu.arizona.sirls.semanticMarkup.eval.matcher.AbstractMatcher;
import edu.arizona.sirls.semanticMarkup.eval.matcher.IMatcher;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Structure;


public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.equalsOrNull("structure name", structureA.getName(), structureB.getName())  &&
				this.equalsOrNull("structure constraint", structureA.getConstraint(), structureB.getConstraint());
		return result;
	}
}
