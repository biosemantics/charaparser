package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Renames a character with a given original name to a given target name
 */
public class RenameCharacter extends AbstractTransformer {

	private Map<String, String> renames;

	public RenameCharacter(Map<String, String> renames) {
		this.renames = renames;
	}
	
	@Override
	public void transform(Document document) {
		for(String original : renames.keySet()) {
			renameCharacter(document, original, renames.get(original));
		}
	}
	
	private void renameCharacter(Document document, String oldName, String newName) {
		for (Element character : this.characterPath.evaluate(document)) {
			if(character.getAttributeValue("name").equals(oldName)) {
				character.setAttribute("name", newName);
			}
		}
	}
	
}
