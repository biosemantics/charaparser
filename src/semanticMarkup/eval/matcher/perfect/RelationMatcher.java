package semanticMarkup.eval.matcher.perfect;

import java.util.Objects;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Relation;


public class RelationMatcher implements IMatcher<Relation> {
	
	private StructureMatcher structureMatcher = new StructureMatcher();
	
	@Override
	public boolean isMatch(Relation relationA, Relation relationB) {
		return relationA.getToStructure() != null && relationB.getToStructure() != null &&
				relationA.getFromStructure() != null && relationB.getFromStructure() != null && 
				structureMatcher.isMatch(relationA.getToStructure(), relationB.getToStructure()) &&
				structureMatcher.isMatch(relationA.getFromStructure(), relationB.getFromStructure()) &&
				Objects.equals(relationA.getName(), relationB.getName()) && 
				Objects.equals(relationA.getNegation(), relationB.getNegation());
	}

}
