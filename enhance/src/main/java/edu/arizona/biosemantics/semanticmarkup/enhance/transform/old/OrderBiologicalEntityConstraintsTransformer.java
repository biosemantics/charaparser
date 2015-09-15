package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class OrderBiologicalEntityConstraintsTransformer extends AbstractTransformer {
	
	
	/**
	 * if a structure has multiple constraints, put them in the natural order they occur in the text
	 * @param result
	 */
	@Override
	public void transform(Document document) {
		for (Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChild("text").getText();
			for (Element biologicalEntity : statement.getChildren("biological_entity")) {
				String constraints = biologicalEntity.getAttributeValue("constraint");
				if(constraints != null && (constraints.contains(";") || constraints.contains(" "))) {
					constraints = order(constraints, sentence, biologicalEntity.getAttributeValue("name_original"));
					biologicalEntity.setAttribute("constraint", constraints);
				}
			}
		}
	}
}
