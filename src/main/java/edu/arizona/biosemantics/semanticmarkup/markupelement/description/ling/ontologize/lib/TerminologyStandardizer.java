/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.inject.Inject;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
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
				
				//standardize character
				LinkedHashSet<Character> characters = struct.getCharacters();
				for(Character character: characters){
					preferedName = null;
					String value = character.getValue().trim();
					if(value!=null && !value.contains(" ") && !character.getName().contains(or)){
						preferedName = characterKnowledgeBase.getCharacterName(value).getLabel(character.getName());
					}
					if(preferedName !=null) character.setValue(preferedName);
				}
			}
		}
	}

}
