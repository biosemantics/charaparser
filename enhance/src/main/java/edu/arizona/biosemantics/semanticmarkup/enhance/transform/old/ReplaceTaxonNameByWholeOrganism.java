package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Replaces first biological entity by a whole_organism entity if it was a taxon_name entity.
 */
public class ReplaceTaxonNameByWholeOrganism extends AbstractTransformer {
	
	/**
	 * some description paragraphs start to name the taxon the organism belongs to. 
	 * e.g. a description about 'Persian cats' starts with 'cats with long hairs and ...'
	 * the markup of such usage of taxon name is normalized to whole_organism here
	 * @param result
	 */
	@Override
	public void transform(Document document) {
		for (Element statement : this.statementXpath.evaluate(document)) {
			List<Element> biologicalEntities = statement.getChildren("biological_entity");
			if(!biologicalEntities.isEmpty()) {
				Element firstElement = biologicalEntities.get(0);
				String type = firstElement.getAttributeValue("type");
				String constraint = firstElement.getAttributeValue("constraint");
				if (type != null && type.equals("taxon_name") && (
						constraint == null || constraint.isEmpty())) {
					firstElement.setAttribute("name", "whole_organism");
					firstElement.setAttribute("name_original", "");
					firstElement.setAttribute("type", "structure");
				}
			}
		}	
	}

}
