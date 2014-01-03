package edu.arizona.sirls.semanticMarkup.markupElement.description.eval.matcher.perfect;

import edu.arizona.sirls.semanticMarkup.eval.matcher.AbstractMatcher;
import edu.arizona.sirls.semanticMarkup.eval.matcher.IMatcher;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Relation;


public class RelationMatcher extends AbstractMatcher implements IMatcher<Relation> {
	
	private StructureMatcher structureMatcher = new StructureMatcher();
	
	@Override
	public boolean isMatch(Relation relationA, Relation relationB) {
		boolean result = this.areNotNull("relation toStructure", relationA.getToStructure(), relationB.getToStructure()) &&
				this.areNotNull("relation fromStructure", relationA.getFromStructure(), relationB.getFromStructure()) &&
				structureMatcher.isMatch(relationA.getToStructure(), relationB.getToStructure()) &&
				structureMatcher.isMatch(relationA.getFromStructure(), relationB.getFromStructure()) &&
				this.equalsOrNull("relation name", relationA.getName(), relationB.getName()) && 
				this.equalsOrNull("relation negation", relationA.getNegation(), relationB.getNegation());
				
		return result;
	}



}
