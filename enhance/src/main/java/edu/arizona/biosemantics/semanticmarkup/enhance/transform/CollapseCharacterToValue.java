package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Represent core character attributes into single string and set as character's value
 */
public class CollapseCharacterToValue extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		for(Element character : this.characterPath.evaluate(document)) {
			String charType = character.getAttributeValue("char_type");
			charType = charType == null ? "" : charType;
			switch(charType) {
			case "range_value":
				collapseRangeValue(character);
				break;
			default:
				collapseDefault(character);
				break;
			}
		}
	}

	private void collapseDefault(Element character) {
		String value = character.getAttributeValue("value");
		value = value == null ? "" : value.trim();
		String constraint = character.getAttributeValue("constraint");
		constraint = constraint == null ? "" : constraint.trim();
		String modifier = character.getAttributeValue("modifier");
		modifier = modifier == null ? "" : modifier.trim();
		
		String newValue = value;
		if(!modifier.isEmpty())
			newValue = modifier + " " + newValue;
		if(!constraint.isEmpty())
			newValue = constraint + " ; " + newValue;
		character.setAttribute("value", newValue);
		character.removeAttribute("from");
		character.removeAttribute("constraint");
		character.removeAttribute("modifier");
	}
	
	private void collapseRangeValue(Element character) {
		String from = character.getAttributeValue("from");
		from = from == null ? "" : from.trim();
		String fromUnit = character.getAttributeValue("from_unit");
		fromUnit = fromUnit == null ? "" : fromUnit.trim();
		String to = character.getAttributeValue("to");
		to = to == null ? "" : to.trim();
		String toUnit = character.getAttributeValue("to_unit");
		toUnit = toUnit == null ? "" : toUnit.trim();
		String constraint = character.getAttributeValue("constraint");
		constraint = constraint == null ? "" : constraint.trim();
		String modifier = character.getAttributeValue("modifier");
		modifier = modifier == null ? "" : modifier.trim();
		
		String newValue = (from + " " + fromUnit).trim() + " - " + (to + " " + toUnit).trim();
		if(!modifier.isEmpty())
			newValue = modifier + " " + newValue;
		if(!constraint.isEmpty())
			newValue = constraint + " ; " + newValue;
		character.setAttribute("value", newValue);
		character.removeAttribute("from");
		character.removeAttribute("from_unit");
		character.removeAttribute("to");
		character.removeAttribute("to_unit");
		character.removeAttribute("constraint");
		character.removeAttribute("modifier");
	}
}