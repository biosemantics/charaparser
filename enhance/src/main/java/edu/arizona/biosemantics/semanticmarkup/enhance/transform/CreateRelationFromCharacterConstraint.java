package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsCharacterConstraintType;


public class CreateRelationFromCharacterConstraint extends AbstractTransformer {

	private KnowsCharacterConstraintType knowsCharacterConstraintType;
	private IInflector inflector;

	public CreateRelationFromCharacterConstraint(KnowsCharacterConstraintType knowsCharacterConstraintType, IInflector inflector) {
		this.knowsCharacterConstraintType = knowsCharacterConstraintType;
		this.inflector = inflector;
	}
	
	@Override
	public void transform(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			String charType = character.getAttributeValue("char_type");
			String constraint = character.getAttributeValue("constraint");
			String constraintid = character.getAttributeValue("constraintid");
			if(value != null)
				value = value.trim();
			if(charType != null)
				charType = charType.trim();
			if(constraint != null)
				constraint = constraint.trim();
			if(constraintid != null)
				constraintid = constraintid.trim();
			
			if(constraintid != null) {
				Element constraintIdElement = this.getBiologicalEntityWithId(document, constraintid);
				if(constraintIdElement != null) {
					String relationName = constraint;
					relationName = relationName.replaceAll("\\b" + inflector.getPlural(constraintIdElement.getAttributeValue("name_original")) + "\\b", "").trim();
					relationName = relationName.replaceAll("\\b" + inflector.getSingular(constraintIdElement.getAttributeValue("name_original")) + "\\b", "").trim();
					relationName = relationName.replaceAll("\\b" + inflector.getPlural(constraintIdElement.getAttributeValue("name")) + "\\b", "").trim();
					relationName = relationName.replaceAll("\\b" + inflector.getSingular(constraintIdElement.getAttributeValue("name")) + "\\b", "").trim();
					Element relation = createRelation((Element)character.getParent(), constraintIdElement, relationName);
					character.getParent().getParent().addContent(relation);
					character.removeAttribute("constraint");
					character.removeAttribute("constraintid");
				}
			}
		}
	}

	private Element createRelation(Element from, Element to, String name) {
		Element relation = new Element("relation");
		relation.setAttribute("from", from.getAttributeValue("id"));
		relation.setAttribute("to", to.getAttributeValue("id"));
		relation.setAttribute("name", name);
		return relation;
	}
	
}
