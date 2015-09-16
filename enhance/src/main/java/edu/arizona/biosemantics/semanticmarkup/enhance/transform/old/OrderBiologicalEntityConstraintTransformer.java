package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Order multiple constraints of a structure in the natural order they occur in the text
 */
public class OrderBiologicalEntityConstraintTransformer extends AbstractTransformer {
	
	@Override
	public void transform(Document document) {
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
