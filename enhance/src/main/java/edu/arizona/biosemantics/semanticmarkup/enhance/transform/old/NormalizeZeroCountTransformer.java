package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class NormalizeZeroCountTransformer extends AbstractTransformer {

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
		for(Element structure : this.biologicalEntityPath.evaluate(document)) {
			for(Element character : structure.getChildren("character")) {
				String name = character.getAttributeValue("name");
				String value = character.getAttributeValue("value");
				String modifier = character.getAttributeValue("modifier");
				if(name != null && name.compareTo("count")==0) {
					if(value != null){
						if(value.compareTo("none") == 0) 
							character.setAttribute("value", "0"); 
						if(value.compareTo("absent") == 0 && (modifier == null || !modifier.matches("no|not|never"))) 
							character.setAttribute("value", "0"); 
						if(value.compareTo("present") == 0 && modifier !=null && modifier.matches("no|not|never")) { 
							character.setAttribute("value", "0");
							character.setAttribute("modifier", "");
						}
					}
				}	
			}
		}
	}
	
}
