package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Replaces quantity = 0 by presence = absent
 */
public class StandardizeQuantityPresence extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		standardizeQuantityPresenceCharacters(document);
	}

	private void standardizeQuantityPresenceCharacters(Document document) {
		for(Element character : this.characterPath.evaluate(document)) {
			//if(c.getName().compareTo("quantity")==0 && c.getValue()!=null && c.getValue().matches(".*?\\b(absent|present|0)\\b.*")){
			String name = character.getAttributeValue("name");
			String value = character.getAttributeValue("value");
			String negation = character.getAttributeValue("negation");
			if(name.equals("quantity") && value != null && value.matches("absent|present|0")) {
				name = "presence";
				value = "absent";
				character.setAttribute("name", name);
				character.setAttribute("value", value.replaceAll("0", value));
			}
			
			if(negation!=null && negation.equals("true") && name.equals("presence") && value.equals("absent")) {
				value = "present";
				negation = "false";
				character.setAttribute("value", value);
				character.setAttribute("negation", negation);
			}
			if(negation!=null && negation.equals("true") && name.equals("presence") && value.equals("present")) {
				value = "absent";
				negation = "false";
				character.setAttribute("value", value);
				character.setAttribute("negation", negation);
			}
		}
	}
}
