package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Normalize count: Replace keywords by actual counts.
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
			if(name != null && name.equals("count")) {
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
