package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Represent core biological entity attributes into single string and set as it's name
 */
public class CollapseBiologicalEntityToName extends AbstractTransformer  {

	@Override
	public void transform(Document document) {
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String newName = collapse(biologicalEntity);
			biologicalEntity.setAttribute("name", newName);
			biologicalEntity.removeAttribute("constraint");
			biologicalEntity.removeAttribute("inferred_constraint");
		}
	}
	
	public String collapse(Element biologicalEntity) {
		String constraint = biologicalEntity.getAttributeValue("constraint");
		constraint = constraint == null ? "" : constraint.trim();
		String name = biologicalEntity.getAttributeValue("name");
		name = name == null ? "" : name.trim();
		if(!constraint.isEmpty())
			name = constraint + " " + name;
		return name;
	}

}
