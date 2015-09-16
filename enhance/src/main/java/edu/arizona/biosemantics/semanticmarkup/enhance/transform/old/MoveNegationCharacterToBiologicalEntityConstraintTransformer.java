package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Remove a count = no character that is placed in the first location of a biological entity.
 * Prepend biological entity's constraint by "no" instead.
 */
public class MoveNegationCharacterToBiologicalEntityConstraintTransformer extends AbstractTransformer {

	/**
	 * turn "no organ" markup to advConstraintedOrgan format
	 * 
	 * <statement id="1_1.txtp4.txt-0">
            <text>No winged queens are known.</text>
      		<structure name="queen" id="o77" notes="structure" name_original="queens">
   			<character name="count" value="no" is_modifier="true"/>
   			<character name="architecture" value="winged" is_modifier="true"/>
			</structure>
          </statement>
	 * 
	 * =>
	 * 
	 * <statement id="1_1.txtp4.txt-0">
            <text>No winged queens are known.</text>
           <structure name="queen" id="o77" notes="structure" name_original="queens" constraint="no">
   			<character name="architecture" value="winged" is_modifier="true"/>
            </structure>
          </statement>
	 * 
	 * move "no" to structure constraint
	 * 
	 * then call normalizeAdvConstraintedOrgan
	 * run these two before normalizeZeroCount.
	 * 
	 * @param xml
	 */
	@Override
	public void transform(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String entityConstraint = biologicalEntity.getAttributeValue("constraint");
			int i = 0;
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String name = character.getAttributeValue("name");
				String value = character.getAttributeValue("value");
				String isModifier = character.getAttributeValue("is_modifier");
				if(i == 0 && name != null && name.equals("count") && value != null
						&& value.equals("no") && isModifier != null && isModifier.equals("true")) {
					String constraint = entityConstraint == null ? "" : entityConstraint;
					biologicalEntity.setAttribute("constraint", ("no " + constraint).trim());
					character.detach();
				}
			}
		}
	}
	
}
