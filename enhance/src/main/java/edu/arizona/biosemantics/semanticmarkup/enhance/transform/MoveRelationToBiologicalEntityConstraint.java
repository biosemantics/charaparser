package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

public class MoveRelationToBiologicalEntityConstraint extends AbstractTransformer {

	private String names = "part_of|in|on|at";
		
	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			for(Element relation : new ArrayList<Element>(statement.getChildren("relation"))) {
				String name = relation.getAttributeValue("name");
				if(name.matches(names)) {
					String from = relation.getAttributeValue("from");
					String to = relation.getAttributeValue("to");
					Element fromElement = this.getBiologicalEntityWithId(document, from);
					Element toElement = this.getBiologicalEntityWithId(document, to);
					
					appendConstraint(fromElement, name + " " + getBiologicalEntityString(toElement));
					relation.detach();
				}
			}
		}
	}

	private String getBiologicalEntityString(Element biologicalEntity) {
		CollapseBiologicalEntityToName collapseBiologicalEntityToName = new CollapseBiologicalEntityToName();
		return collapseBiologicalEntityToName.collapse(biologicalEntity);
	}

}
