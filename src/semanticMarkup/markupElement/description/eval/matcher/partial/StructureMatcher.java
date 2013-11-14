package semanticMarkup.markupElement.description.eval.matcher.partial;

import semanticMarkup.eval.matcher.AbstractMatcher;
import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.markupElement.description.model.Structure;

public class StructureMatcher extends AbstractMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		boolean result = this.valuesNullOrContainedEitherWay("name", structureA.getName(), structureB.getName());
		return result;
	}
}
