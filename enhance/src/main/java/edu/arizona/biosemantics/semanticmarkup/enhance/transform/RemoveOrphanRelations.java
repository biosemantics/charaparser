package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Removes relations that have a "to" or "from" entity that does not exist in the document
 * 
 */
public class RemoveOrphanRelations extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		for(Element relation : this.relationPath.evaluate(document)) {
			String from = relation.getAttributeValue("from");
			String to = relation.getAttributeValue("to");
			if(from == null || from.isEmpty() || to == null || to.isEmpty()) {
				relation.detach();
				continue;
			}
			
			Element fromEntity = this.getBiologicalEntityWithId(document, from);
			Element toEntity = this.getBiologicalEntityWithId(document, to);
			if(fromEntity == null || toEntity == null)
				relation.detach();
		}
	}

}
