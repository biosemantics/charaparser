package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class CharacterConstraintTransformer extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		removeCircularCharacterConstraint(document);
		orderConstraintsNaturally(document);
	}
	
	/**
	 * if a character constraint refers to the same structure the character belongs to, remove the constraint
	 * @param result
	 */
	private void removeCircularCharacterConstraint(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String oid = biologicalEntity.getAttributeValue("id");
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String constraintId = character.getAttributeValue("constraintid");
				if(constraintId != null && constraintId.matches(".*?\\b" + oid + "\\b.*")) {
					character.removeAttribute("constraint");
					character.removeAttribute("constraintid");
				}
			}
		}
	}
	
	/**
	 * if a structure has multiple constraints, put them in the natural order they occur in the text
	 * @param result
	 */
	private void orderConstraintsNaturally(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChildText("text");
			for(Element structure : statement.getChildren("biological_entity")) {
				String constraints = structure.getAttributeValue("constraint");
				String originalName = structure.getAttributeValue("name_original");
				if(constraints != null && (constraints.contains(";") || constraints.contains(" "))) {
					constraints = order(constraints, sentence, originalName);;
					structure.setAttribute("constraint", constraints);
				}
			}
		}
	}	
}
