package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class CharacterToStructureConstraintTransformer extends AbstractTransformer {
	

	@Override
	public void transform(Document document) {
		for (Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String isModifier = character.getAttributeValue("is_modifier");
				
				if(isModifier != null && isModifier.equals("true") && character.getAttributeValue("name") != null &&
						character.getAttributeValue("name").matches(".*?(^|_or_)(" + ElementRelationGroup.entityStructuralConstraintElements + ")(_or_|$).*")) {
					if(character.getAttributeValue("value") != null) {
						String appendConstraint = character.getAttributeValue("value");
						
						String newValue = "";
						String constraint = character.getAttributeValue("constraint");
						if(constraint != null && !constraint.matches(".*?(^|; )"+appendConstraint+"($|;).*")) {
							newValue = constraint + "; " + appendConstraint;
						}
						biologicalEntity.setAttribute("constraint", newValue);
						character.detach();
					}
				}
			}
		}
	}
}
