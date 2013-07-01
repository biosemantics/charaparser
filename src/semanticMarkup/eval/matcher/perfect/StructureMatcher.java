package semanticMarkup.eval.matcher.perfect;

import java.util.Objects;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Structure;
import semanticMarkup.eval.matcher.AbstractMatcher;


public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.equalsOrNull("structure name", structureA.getName(), structureB.getName())  &&
				this.equalsOrNull("structure constraint", structureA.getConstraint(), structureB.getConstraint());
		return result;
	}
}
