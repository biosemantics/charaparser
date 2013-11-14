package semanticMarkup.markupElement.description.eval.matcher.perfect;

import semanticMarkup.eval.matcher.AbstractMatcher;
import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.markupElement.description.model.Structure;


public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.equalsOrNull("structure name", structureA.getName(), structureB.getName())  &&
				this.equalsOrNull("structure constraint", structureA.getConstraint(), structureB.getConstraint());
		return result;
	}
}
