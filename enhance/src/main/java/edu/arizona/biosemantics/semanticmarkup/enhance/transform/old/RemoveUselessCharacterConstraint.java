package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Remove character constraints that refer to the parent structure of the character
 * @author rodenhausen
 *
 */
public class RemoveUselessCharacterConstraint extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		//What if there is more than one constraint? e.g. separated by ;
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String id = biologicalEntity.getAttributeValue("id");
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String constraintId = character.getAttributeValue("constraintid");
				if(constraintId != null && constraintId.matches(".*?\\b" + id + "\\b.*")) {
					character.removeAttribute("constraint");
					character.removeAttribute("constraintid");
				}
			}
		}
	}
}
