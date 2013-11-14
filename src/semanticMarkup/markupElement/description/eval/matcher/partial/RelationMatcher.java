package semanticMarkup.markupElement.description.eval.matcher.partial;

import semanticMarkup.eval.matcher.AbstractMatcher;
import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.markupElement.description.model.Relation;

public class RelationMatcher extends AbstractMatcher implements IMatcher<Relation> {

	private StructureMatcher structureMatcher = new StructureMatcher();
	
	@Override
	public boolean isMatch(Relation relationA, Relation relationB) {
		boolean result = this.areNotNull("relation toStructure", relationA.getToStructure(), relationB.getToStructure()) && 
				this.areNotNull("relation fromStructure", relationA.getFromStructure(), relationB.getFromStructure()) && 
				structureMatcher.isMatch(relationA.getToStructure(), relationB.getToStructure()) &&
				structureMatcher.isMatch(relationA.getFromStructure(), relationB.getFromStructure()) &&
				this.valuesNullOrContainedEitherWay("name", relationA.getName(), relationB.getName()) &&
				this.equalsOrNull("negation", relationA.getNegation(), relationB.getNegation());
		return result;
	}

}
