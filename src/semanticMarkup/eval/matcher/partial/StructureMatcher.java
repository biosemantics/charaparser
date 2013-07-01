package semanticMarkup.eval.matcher.partial;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Structure;
import semanticMarkup.eval.matcher.AbstractMatcher;

public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.valuesNullOrContainedEitherWay("name", structureA.getName(), structureB.getName());
		return result;
	}
}
