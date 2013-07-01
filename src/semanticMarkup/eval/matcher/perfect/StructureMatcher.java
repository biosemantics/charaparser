package semanticMarkup.eval.matcher.perfect;

import java.util.Objects;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Structure;


public class StructureMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		return Objects.equals(structureA.getName(), structureB.getName()) &&
				Objects.equals(structureA.getConstraint(), structureB.getConstraint());
	}

}
