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
			String constraint = biologicalEntity.getAttributeValue("constraint");
			constraint = constraint == null ? "" : constraint.trim();
			String name = biologicalEntity.getAttributeValue("name");
			name = name == null ? "" : name.trim();
			String newName = name;
			if(!constraint.isEmpty())
				newName = constraint + " " + newName;
			biologicalEntity.setAttribute("name", newName);
			biologicalEntity.removeAttribute("constraint");
		}
	}

}
