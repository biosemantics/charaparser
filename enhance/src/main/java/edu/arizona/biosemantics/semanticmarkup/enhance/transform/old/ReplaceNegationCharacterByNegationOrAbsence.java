// Rewrite this with the sample we made up: No red leaves toothed; No winged queens known/present.

package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Remove a count = no character that is placed in the first location of a biological entity.
 * Prepend biological entity's constraint by "no" instead.
 * 
 * /**
//	 * turn "no organ" markup to advConstraintedOrgan format
//	 * 
//	 * <statement id="1_1.txtp4.txt-0">
//          <text>No winged queens are known.</text>
//      	<structure name="queen" id="o77" notes="structure" name_original="queens">
//   			<character name="count" value="no" is_modifier="true"/>
//   			<character name="architecture" value="winged" is_modifier="true"/>
//			</structure>
//          </statement>
//	 * 
//	 * =>
//	 * 
//	 * <statement id="1_1.txtp4.txt-0">
//            <text>No winged queens are known.</text>
//           <structure name="queen" id="o77" notes="structure" name_original="queens" constraint="no">
//   			<character name="architecture" value="winged" is_modifier="true"/>
//            </structure>
//          </statement>
//	 * 
//	 * move "no" to structure constraint
 * 
 * Examples:
 * 
 * No red leaves toothed
 * no winged queens present/known
 * [Negation] [is_modifier character] [organ] [character, character, ...]
 * 
 *  winged queen -> not present (-> absent ; separate transformer) is the desired output
 *  red leavess -> not toothed desired output
 *  
 *  Assumption: 
 *  - Clean separation between real modifying characters and no such is performed before
 *  - is modifier characters have already been applied to biological entity constraint if applicable
 */
public class ReplaceNegationCharacterByNegationOrAbsence extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {			
			Element countNoCharacter = getCountNoCharacter(biologicalEntity);
			if(countNoCharacter != null) {
				List<Element> noModifierCharacters = getNoModifierCharacters(biologicalEntity);
				List<Element> modifierCharacters = getModifierCharacters(biologicalEntity);
				
				if(noModifierCharacters.isEmpty()) {
					removeCountNoCharacter(countNoCharacter, biologicalEntity);
					addAbsentCharacter(biologicalEntity);
				} else {
					removeCountNoCharacter(countNoCharacter, biologicalEntity);
					negateCharacters(noModifierCharacters);
				}
			}
		}
	}

	private void negateCharacters(List<Element> characters) {
		for(Element character : characters) {
			character.setAttribute("negation", "true");
		}
	}

	private List<Element> getModifierCharacters(Element biologicalEntity) {
		List<Element> characters = new ArrayList<Element>(biologicalEntity.getChildren("character"));
		Iterator<Element> iterator = characters.iterator();
		while(iterator.hasNext()) {
			Element character = iterator.next();
			String isModifier = character.getAttributeValue("is_modifier");
			if(isModifier == null)
				iterator.remove();
			if(isModifier != null && isModifier.equals("false")) 
				iterator.remove();
		}
		return characters;
	}

	private List<Element> getNoModifierCharacters(Element biologicalEntity) {
		List<Element> characters = new ArrayList<Element>(biologicalEntity.getChildren("character"));
		Iterator<Element> iterator = characters.iterator();
		while(iterator.hasNext()) {
			Element character = iterator.next();
			String isModifier = character.getAttributeValue("is_modifier");
			if(isModifier != null && isModifier.equals("true")) 
				iterator.remove();
		}
		return characters;
	}

	private void addAbsentCharacter(Element biologicalEntity) {
		Element character = new Element("character");
		character.setAttribute("name", "presence");
		character.setAttribute("value", "absent");
		biologicalEntity.addContent(character);
	}

	private void removeCountNoCharacter(Element countNoCharacter, Element biologicalEntity) {
		countNoCharacter.detach();
	}

	private Element getCountNoCharacter(Element biologicalEntity) {
		for(Element character : biologicalEntity.getChildren("character")) {
			if(character.getAttributeValue("name").equals("count") && (character.getAttributeValue("value")!=null && character.getAttributeValue("value").equals("no")) &&
					(character.getAttributeValue("is_modifier")!=null && character.getAttributeValue("is_modifier").equals("true"))) {
				return character;
			}
		}
		return null;
	}
}
