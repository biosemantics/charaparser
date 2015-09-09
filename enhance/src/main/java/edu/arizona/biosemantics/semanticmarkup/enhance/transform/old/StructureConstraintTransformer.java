package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class StructureConstraintTransformer extends AbstractTransformer {

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
	private void noOrgan2AdvConstraintedOrgan(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String entityConstraint = biologicalEntity.getAttributeValue("constraint");
			int i = 0;
			for(Element character: new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String name = character.getAttributeValue("name");
				String value = character.getAttributeValue("value");
				String isModifier = character.getAttributeValue("is_modifier");
				String constrailnt = character.getAttributeValue("constraint");
				if(i==0 && name != null && name.equals("count") && value != null
						&& value.equals("no") && isModifier != null && isModifier.equals("true")) {
					String constraint = entityConstraint == null ? "" : entityConstraint;
					biologicalEntity.setAttribute("constraint", ("no " + constraint).trim());
					character.detach();
				}
			}
		}
	}
	
	private void character2structureContraint(Document document) {
		for (Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String isModifier = character.getAttributeValue("is_modifier");
				
				if(isModifier != null && isModifier.equals("true") && character.getAttributeValue("name") != null &&
						character.getAttributeValue("name").matches(".*?(^|_or_)(" + ElementRelationGroup.entityStructuralConstraintElements + ")(_or_|$).*")) {
					if(character.getAttributeValue("value") != null) {
						String appendConstraint = character.getAttributeValue("value");
						
						String newValue = "";
						String constraint = character.getAttributeValue("constraint");
						if(constraint != null && !constraint.matches(".*?(^|; )"+appendConstraint+"($|;).*")){
							newValue = constraint + "; " + appendConstraint;
						}
						biologicalEntity.setAttribute("constraint", newValue);
						character.detach();
					}
				}
			}
		}
	}
	
	/**
	 * if a structure has multiple constraints, put them in the natural order they occur in the text
	 * @param result
	 */
	private void phraseUpConstraints(Document document) {
		for (Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChild("text").getText();
			for (Element biologicalEntity : statement.getChildren("biological_entity")) {
				String constraints = biologicalEntity.getAttributeValue("constraint");
				if(constraints != null && (constraints.contains(";") || constraints.contains(" "))){
					constraints = order(constraints, sentence, biologicalEntity.getAttributeValue("name_original"));
					biologicalEntity.setAttribute("constraint", constraints);
				}
			}
		}
	}

	@Override
	public void transform(Document document) {
		noOrgan2AdvConstraintedOrgan(document);
		character2structureContraint(document);
		phraseUpConstraints(document);
	}
}
