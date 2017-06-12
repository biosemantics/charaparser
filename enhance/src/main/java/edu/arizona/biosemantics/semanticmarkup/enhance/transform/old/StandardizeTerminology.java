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
			if(preferedName!=null && !preferedName.equals(name)){ 
				biologicalEntity.setAttribute("name", preferedName.replaceAll("_", "-"));
				if(biologicalEntity.getAttribute("name_original")== null)
					biologicalEntity.setAttribute("name_original", name);
			}
			
			//standardize structural constraint, a word or a phrase, e.g. "between eyes and nose"
			//String constraint = struct.getConstraint(); //try to match longest segment anchored to the last word in the phrase.
			if(constraint != null){
				constraint = constraint.trim(); 
				if(constraint.startsWith("between")){
					String[] terms = constraint.replaceFirst("between", "").trim().split("\\s+(and|,)\\s+");
					String prefered = "between ";
					for(int i = 0; i < terms.length; i++){
						String pref = characterKnowledgeBase.getCharacterName(terms[i]).getLabel(type);
						if(pref!=null) 
							if(i == terms.length-2) prefered = prefered+" "+pref + " and ";
							else prefered = prefered+" "+pref + ", ";
						else
							if(i == terms.length-2) prefered = prefered+" "+terms[i] + " and ";
							else prefered = prefered+" "+terms[i] + ", ";
					}
					biologicalEntity.setAttribute("constraint", prefered.replaceFirst(", $", "").replaceAll("_", "-").replaceAll("\\s+", " "));
				}else{
					String prefered = characterKnowledgeBase.getCharacterName(constraint).getLabel(type);
					if(prefered != null && !prefered.equals(constraint)){
						biologicalEntity.setAttribute("constraint", prefered.replaceAll("_", "-"));
						if(biologicalEntity.getAttribute("constraint_original")== null)
							biologicalEntity.setAttribute("constraint_original", constraint);
					}
				}
			}
			
			//standardize single-word character values
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String preferedValue = null;
				String value = character.getAttributeValue("value");
				if(value != null && !value.trim().contains(" ") && !character.getAttributeValue("name").contains(or)){
					preferedValue = characterKnowledgeBase.getCharacterName(value.trim()).getLabel(character.getAttributeValue("name"));
				}
				if(preferedValue != null && !preferedValue.equals(value)){ 
					character.setAttribute("value", preferedValue.replaceAll("_", "-"));
					if(character.getAttribute("value_original")== null)
						character.setAttribute("value_original", value);
				}
			}
		}
	}

}
