package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Replace the following by preferred terms as determined by a characterKnowledgeBase.
 * - biological entity name
 * - biological entity constraint
 * - character value
 */
public class StandardizeTerminology extends AbstractTransformer {

	private ICharacterKnowledgeBase characterKnowledgeBase;
	private String or = "_or_";
	
	public StandardizeTerminology(ICharacterKnowledgeBase characterKnowledgeBase){
		this.characterKnowledgeBase = characterKnowledgeBase;
	}
	
	@Override
	public void transform(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String type = biologicalEntity.getAttributeValue("type");
			String name = biologicalEntity.getAttributeValue("name");
			String constraint = biologicalEntity.getAttributeValue("constraint");
			String preferedName = characterKnowledgeBase.getCharacterName(name).getLabel(type);
			if(preferedName != null) 
				biologicalEntity.setAttribute("name", preferedName.replaceAll("_", "-"));
			
			//standardize structural constraint, a word or a phrase
			//String constraint = struct.getConstraint(); //try to match longest segment anchored to the last word in the phrase.
			if(constraint != null){
				constraint = constraint.trim();
				String prefered = characterKnowledgeBase.getCharacterName(constraint).getLabel(type);
				if(prefered != null){
					biologicalEntity.setAttribute("constraint", prefered.replaceAll("_", "-"));
				}
			}
			
			//standardize character
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				preferedName = null;
				String value = character.getAttributeValue("value");
				if(value != null && !value.trim().contains(" ") && !character.getAttributeValue("name").contains(or)){
					preferedName = characterKnowledgeBase.getCharacterName(value.trim()).getLabel(character.getAttributeValue("name"));
				}
				if(preferedName != null) 
					character.setAttribute("value", preferedName.replaceAll("_", "-"));
			}
		}
	}

}
