package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Moves constraint information represented by a character (when character matches certain pattern, e.g. is_modifier, name)  to its parent biological entity
 */
public class MoveCharacterToStructureConstraint extends AbstractTransformer {
	
	@Override
	public void transform(Document document) {
		for (Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String isModifier = character.getAttributeValue("is_modifier");
				
				if(isModifier != null && isModifier.equals("true") && character.getAttributeValue("name") != null &&
						character.getAttributeValue("name").matches(".*?(^|_or_)(" + ElementRelationGroup.entityStructuralConstraintElements + ")(_or_|$).*")) {
					if(character.getAttributeValue("value") != null) {
						String appendConstraint = character.getAttributeValue("value");
						
						String constraint = character.getAttributeValue("constraint");
						constraint = constraint == null ? "" : constraint;
						if(!constraint.matches(".*?(^|; )"+appendConstraint+"($|;).*")) {
							if(constraint.isEmpty()) {
								constraint = appendConstraint;
							} else {
								constraint = constraint + "; " + appendConstraint;
							}
						}
						biologicalEntity.setAttribute("constraint", constraint);
						character.detach();
					}
				}
			}
		}
	}
}
