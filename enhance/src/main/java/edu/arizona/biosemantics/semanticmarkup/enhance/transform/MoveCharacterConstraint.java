package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;


public class MoveCharacterConstraint extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			String charType = character.getAttributeValue("char_type");
			String constraint = character.getAttributeValue("constraint");
			if(value != null)
				value = value.trim();
			if(charType != null)
				charType = charType.trim();
			if(constraint != null)
				constraint = constraint.trim();
			
			//check on constraint to decide what to do 
			//(1) move to value
			character.setAttribute("value", "new value");
			
			//(2) create as relation
			Element statement = character.getParentElement().getParentElement();
			Element relation = new Element("relation");
			relation.setAttribute("name", "new name");
			//...
			statement.addContent(relation);
			
			//remove
			character.setAttribute("constraint", "");
		}
	}


	
}
