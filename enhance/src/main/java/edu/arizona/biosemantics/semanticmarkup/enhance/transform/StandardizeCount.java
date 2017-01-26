package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Normalize count: Replace keywords (absent) by actual counts (0)
 * 
 * 
 * not present = absent
 * not absent = present
 * deals with negation modifiers (another class deals with negation in negation attributes)
 * count should be quantity.
 */
public class StandardizeCount extends AbstractTransformer {

	/**
	 * 	nomarlization count
	 *  count = "none" =>count = 0; 
	 *  count = "absent" =>count = 0;
	 *  count = "present", modifier = "no|not|never" =>count = 0;
	 *  
	 * @param xml
	 */
	@Override
	public void transform(Document document) {
		for(Element character : this.characterPath.evaluate(document)) {
			String name = character.getAttributeValue("name");
			String value = character.getAttributeValue("value");
			String modifier = character.getAttributeValue("modifier");
			if(name != null && name.equals("quantity")) {
				if(value != null){
					if(value.equals("none")) 
						character.setAttribute("value", "0"); 
					if(value.equals("absent") && (modifier == null || !modifier.matches("no|not|never"))) 
						character.setAttribute("value", "0"); 
					if(value.equals("present") && modifier !=null && modifier.matches("no|not|never")) { 
						character.setAttribute("value", "0");
						character.setAttribute("modifier", "");
					}
				}
			}	
		}
	}
	
}
