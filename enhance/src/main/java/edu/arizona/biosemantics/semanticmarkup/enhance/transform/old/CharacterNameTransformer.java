package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class CharacterNameTransformer extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		renameCharacter(document, "count", "quantity");
		renameCharacter(document, "atypical_count", "atypical_quantity");
		renameCharacter(document, "color", "coloration");
	}
	
	private void renameCharacter(Document document, String oldName, String newName) {
		for (Element character : this.characterPath.evaluate(document)) {
			if(character.getAttributeValue("name").equals(oldName)) {
				character.setAttribute("name", newName);
			}
		}
	}

	
}
