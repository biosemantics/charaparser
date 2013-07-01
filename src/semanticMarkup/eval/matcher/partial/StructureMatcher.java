package semanticMarkup.eval.matcher.partial;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Structure;

public class StructureMatcher implements IMatcher<Structure> {

	@Override
	public boolean isMatch(Structure structureA, Structure structureB) {
		return (structureA.getName() == null && structureB.getName() == null) || 
				((structureA.getName() != null && structureB.getName() != null) && 
				(structureA.getName().contains(structureB.getName()) ||
				structureB.getName().contains(structureA.getName())));
	}

}
