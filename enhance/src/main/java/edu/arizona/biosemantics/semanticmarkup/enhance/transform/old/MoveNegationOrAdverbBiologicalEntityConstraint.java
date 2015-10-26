package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * If negation in biological entity constraint
 * - Move character to constraint if is_modifier; else: Negate character
 * - Negate relations
 * - Add count=0 character if no relation or true character was present
 * - Remove negation from constraint
 * If no negation in biological entity but adverb constraint
 * - Move character to constraint if is_modifier; else:  Add constraint of biologicalEntity to modifier of character
 * - Add constraint of biologicalEntity to modifier of relations
 * - Add count=0 character if no relation or true character was present
 * - Remove adverb constraint from biological entity
 */
public class MoveNegationOrAdverbBiologicalEntityConstraint extends AbstractTransformer {

	private IPOSKnowledgeBase posKnowledgeBase;
	private String adverbsToProcess = "\\b(always|often|seldom|sometimes|[a-z]+ly)\\b";

	public MoveNegationOrAdverbBiologicalEntityConstraint(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}
	
	@Override
	public void transform(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String constraint = biologicalEntity.getAttributeValue("constraint");
			constraint = constraint == null ? "" : constraint; 
			
			String type = biologicalEntity.getAttributeValue("type");
			
			if(type !=null && type.equals("structure")
					//&& ((BiologicalEntity)element).getConstraint()!=null && ((BiologicalEntity)element).getConstraint().matches("^(no|not|never)\\b.*")){
					&& constraint.matches("(no|not|never)")) {//constraint is a single adv
				handleNegatedStructure(document, biologicalEntity, constraint);
				
			} else if(type !=null && type.equals("structure")
					//&& ((BiologicalEntity)element).getConstraint()!=null && posKnowledgeBase.isAdverb(constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint)){
					&& !constraint.isEmpty() && !constraint.contains(" ") && !constraint.contains(";") && constraint.matches(this.adverbsToProcess)) { // This would be too broad: e.g. false is considered adverb. posKnowledgeBase.isAdverb(constraint)) { //constraint is a single adverb
				handleAdverbConstraint(document, biologicalEntity, constraint);
			}
		}
	}

	private void handleAdverbConstraint(Document document, Element biologicalEntity, String constraint) {
		//other advs, mirrors the process above
		String modifier = constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint;
		
		//handle is_modifier characters and true characters
		boolean hasTrueCharacters = handleTrueCharacters(biologicalEntity, constraint, modifier);

		//negate relations
		boolean hasModifiedRelation = modifyRelation(biologicalEntity, document, modifier);

		//no relation and no true character, set count to 0
		if(!hasModifiedRelation && !hasTrueCharacters){
			Element count = new Element("character");
			count.setAttribute("name", "count");
			count.setAttribute("value", "present");
			count.setAttribute("modifier", modifier);
			biologicalEntity.addContent(count);
		}

		constraint = biologicalEntity.getAttributeValue("constraint");
		//remove no|not|never from the structure constraint
		constraint = constraint.replaceFirst("^"+modifier+"\\b", "");
		biologicalEntity.setAttribute("constraint", constraint.trim());
	}

	private boolean handleTrueCharacters(Element biologicalEntity, String constraint, String modifier) {
		boolean hasTrueCharacters = false;
		for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
			String isModifier = character.getAttributeValue("is_modifier");
			if(isModifier != null && isModifier.equals("true")) {
				moveCharacterToConstraint(biologicalEntity, character, constraint);
			}else{
				hasTrueCharacters = true;
				modifyTrueCharacter(character, modifier);
			}
		}
		return hasTrueCharacters;
	}

	private boolean modifyRelation(Element biologicalEntity, Document document, String mod) {
		boolean hasModifiedRelation = false;
		List<Element> relations = this.getRelationsInvolve(biologicalEntity, document);
		for(Element relation : relations) {
			hasModifiedRelation = true;
			String modifier = relation.getAttributeValue("modifier");
			modifier = modifier == null ? "": modifier;
			modifier = mod + " " + modifier;
			relation.setAttribute("modifier", modifier.trim());
		}
		return hasModifiedRelation;
	}

	private void modifyTrueCharacter(Element character, String mod) {
		//modify true characters
		String modifier = character.getAttributeValue("modifier"); 
		modifier = modifier == null ? "": modifier;
		modifier = mod + " " + modifier;
		character.setAttribute("modifier", modifier.trim());
	}

	private void handleNegatedStructure(Document document, Element biologicalEntity, String constraint) {
		//adv is negation
		//handle is_modifier characters and true characters
		boolean hasTrueCharacters = handleModifiedAndTrueCharactersFromNegatedStructure(biologicalEntity, constraint);
		//negate relations
		boolean hasNegatedRelations = negateRelations(biologicalEntity, document);

		//no relation and no true character, set count to 0
		if(!hasNegatedRelations && !hasTrueCharacters){
			Element count = new Element("character");
			count.setAttribute("name", "count");
			count.setAttribute("value", "0");
			biologicalEntity.addContent(count);
		}
		
		//remove no|not|never from the structure constraint
		constraint = biologicalEntity.getAttributeValue("constraint");
		biologicalEntity.setAttribute("constraint", constraint.replaceFirst("^no|not|never\\b", "").trim());
	}

	private boolean handleModifiedAndTrueCharactersFromNegatedStructure(Element biologicalEntity, String constraint) {
		boolean hasTrueCharacters = false;
		for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
			String isModifier = character.getAttributeValue("is_modifier");
			if(isModifier != null && isModifier.equals("true")) {
				moveCharacterToConstraint(biologicalEntity, character, constraint);
			} else {
				hasTrueCharacters = true;
				negateCharacter(character);
			}
		}
		return hasTrueCharacters;
	}

	private boolean negateRelations(Element biologicalEntity, Document document) {
		boolean negatedRelation = false;
		for(Element relation : this.getRelationsInvolve(biologicalEntity, document)) {
			negatedRelation = true;
			String negation = relation.getAttributeValue("negation");
			if(negation !=null && negation.equals("true")) 
				relation.setAttribute("negation", "false");
			else 
				relation.setAttribute("negation", "true");
		}
		return negatedRelation;
	}

	private void negateCharacter(Element character) {
		String modifier = character.getAttributeValue("modifier");
		if (modifier == null) {
			character.setAttribute("modifier", "not");
		} else if (modifier.matches(".*\\bnot\\b.*")) {// double negation
			modifier = modifier.replaceFirst("\\bnot\\b", "");
			character.setAttribute("modifier", modifier.replaceAll("\\s+", " ")
					.trim());
		} else {
			modifier = "not " + modifier;
			character.setAttribute("modifier", modifier.trim());
		}
	}

	private void moveCharacterToConstraint(Element biologicalEntity, Element character, String constraint) {
		//turn this character to structure constraint			
		biologicalEntity.setAttribute("constraint", (constraint + " " + character.getAttributeValue("value").trim()));
		character.detach();
	}	
}
