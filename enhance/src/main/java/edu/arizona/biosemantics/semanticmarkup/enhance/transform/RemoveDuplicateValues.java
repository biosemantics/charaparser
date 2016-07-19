package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Remove characters with a [duplicate value] note.
 */
public class RemoveDuplicateValues extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		for(Element character : this.characterPath.evaluate(document)) {
			String notes = character.getAttributeValue("notes");
			if(notes != null) {
				if(notes.equals("[duplicate value]")) {
					character.detach();
				}
			}
		}
	}

}
