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
			switch(character.getAttributeValue("char_type")) {
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
	}
	
	private void collapseRangeValue(Element character) {
		String from = character.getAttributeValue("from");
		from = from == null ? "" : from.trim();
		String fromUnit = character.getAttributeValue("from_unit");
		from = fromUnit == null ? "" : fromUnit.trim();
		String to = character.getAttributeValue("to");
		from = to == null ? "" : to.trim();
		String toUnit = character.getAttributeValue("to_unit");
		from = toUnit == null ? "" : toUnit.trim();
		String constraint = character.getAttributeValue("constraint");
		from = constraint == null ? "" : constraint.trim();
		String modifier = character.getAttributeValue("modifier");
		from = modifier == null ? "" : modifier.trim();
		
		String newValue = from + " " + fromUnit + " - " + to + " " + toUnit;
		if(!modifier.isEmpty())
			newValue = modifier + " " + newValue;
		if(!constraint.isEmpty())
			newValue = constraint + " ; " + newValue;
		character.setAttribute("value", newValue);
	}
}