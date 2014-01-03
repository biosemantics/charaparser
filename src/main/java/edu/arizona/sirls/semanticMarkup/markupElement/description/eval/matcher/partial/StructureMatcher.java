package edu.arizona.sirls.semanticMarkup.markupElement.description.eval.matcher.partial;

import edu.arizona.sirls.semanticMarkup.eval.matcher.AbstractMatcher;
import edu.arizona.sirls.semanticMarkup.eval.matcher.IMatcher;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Structure;

public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.valuesNullOrContainedEitherWay("name", structureA.getName(), structureB.getName());
		return result;
	}
}
