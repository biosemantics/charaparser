/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.inject.Inject;

import edu.arizona.biosemantics.semanticmarkup.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
/**
 * @author Hong Cui
 * 1. replace synonyms to preferred terms
 */
public class TerminologyStandardizer {
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private String or = "_or_";
	
	@Inject
	public TerminologyStandardizer(ICharacterKnowledgeBase characterKnowledgeBase){
		this.characterKnowledgeBase = characterKnowledgeBase;
	}
	
	public void standardize (List<Element> input){
		for(Element element: input){
			if(element.isStructure()){
				//standardize structure
				BiologicalEntity struct = (BiologicalEntity)element;
				//String preferedName = characterKnowledgeBase.getCharacterName(struct.getName()).getLabel("structure");
				String preferedName = characterKnowledgeBase.getCharacterName(struct.getName()).getLabel(struct.getType());
				if(preferedName!=null) struct.setName(preferedName);
				
				//standardize structural constraint, a word or a phrase
				String constraint = struct.getConstraint(); //try to match longest segment anchored to the last word in the phrase.
				if(constraint!=null){
					constraint = constraint.trim();
					String leading = "";
					do{
						String prefered = characterKnowledgeBase.getCharacterName(constraint).getLabel(struct.getType());
						if(prefered!=null){
							struct.setConstraint((leading+" "+prefered).trim());
							break;
						}else{
							//remove the leading word
							leading = constraint.replaceFirst(" .*", "").trim();
							constraint = constraint.replaceFirst(leading, "").trim(); 
						}
					}while(!constraint.isEmpty());
				}
				
				
				//standardize character
				LinkedHashSet<Character> characters = struct.getCharacters();
				for(Character character: characters){
					preferedName = null;
					String value = character.getValue();
					if(value!=null && !value.trim().contains(" ") && !character.getName().contains(or)){
						preferedName = characterKnowledgeBase.getCharacterName(value.trim()).getLabel(character.getName());
					}
					if(preferedName !=null) character.setValue(preferedName);
				}
			}
		}
	}

}
