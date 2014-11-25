/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.inject.Inject;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
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
				Structure struct = (Structure)element;
				//String preferedName = characterKnowledgeBase.getCharacterName(struct.getName()).getLabel("structure");
				String preferedName = characterKnowledgeBase.getCharacterName(struct.getName()).getLabel(((Structure)element).getNotes());
				if(preferedName!=null) struct.setName(preferedName);
				
				//standardize character
				LinkedHashSet<Character> characters = struct.getCharacters();
				for(Character character: characters){
					String value = character.getValue();
					if(value!=null && !character.getName().contains(this.or)){
						preferedName = characterKnowledgeBase.getCharacterName(value).getLabel(character.getName());
						if(preferedName !=null) character.setValue(preferedName);
					}
				}
			}
		}
	}

}
