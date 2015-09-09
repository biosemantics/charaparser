package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class NormalizeAdverbConstraintedOrganTransformer extends AbstractTransformer {

	private IPOSKnowledgeBase posKnowledgeBase;

	public NormalizeAdverbConstraintedOrganTransformer(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}
	
	@Override
	public void transform(Document document) {
		normalizeAdvConstraintedOrgan(document);
	}
	
	private void normalizeAdvConstraintedOrgan(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String constraint = biologicalEntity.getAttributeValue("constraint");
			constraint = constraint == null ? "" : constraint; 
			
			String type = biologicalEntity.getAttributeValue("type");
			
			if(type !=null && type.equals("structure")
					//&& ((BiologicalEntity)element).getConstraint()!=null && ((BiologicalEntity)element).getConstraint().matches("^(no|not|never)\\b.*")){
					&& constraint.matches("(no|not|never)")){//constraint is a single adv
				//adv is negation
				//handle is_modifier characters and true characters
				boolean hasTrueCharacters = false;
				for(Element character: new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
					String isModifier = character.getAttributeValue("is_modifier");
					if(isModifier != null && isModifier.equals("true")) {
						//turn this character to structure constraint			
						biologicalEntity.setAttribute("constraint", (constraint + " " + character.getAttributeValue("value").trim()));
						character.detach();
					} else {
						//negate true characters
						hasTrueCharacters = true;
						String modifier = character.getAttributeValue("modifier");
						if(modifier == null){
							character.setAttribute("modifier", "not");
						}else if(modifier.matches(".*\\bnot\\b.*")){//double negation 
							modifier = modifier.replaceFirst("\\bnot\\b", "");
							character.setAttribute("modifier", modifier.replaceAll("\\s+", " ").trim());
						}else{
							modifier ="not "+modifier;
							character.setAttribute("modifier", modifier.trim());
						}
					}
				}
				//negate relations
				boolean negatedRelation = false;
				for(Element relation : this.getRelationsInvolve(biologicalEntity, document)) {
					negatedRelation = true;
					String negation = relation.getAttributeValue("negation");
					if(negation !=null && negation.equals("true")) 
						relation.setAttribute("negation", "false");
					else 
						relation.setAttribute("negation", "true");
				}

				//no relation and no true character, set count to 0
				if(!negatedRelation && !hasTrueCharacters){
					Element count = new Element("character");
					count.setAttribute("name", "count");
					count.setAttribute("value", "0");
					biologicalEntity.addContent(count);
				}

				//remove no|not|never from the structure constraint
				constraint = biologicalEntity.getAttributeValue("constraint");
				biologicalEntity.setAttribute("constraint", constraint.replaceFirst("^no|not|never\\b", "").trim());
			} else if(type !=null && type.equals("structure")
					//&& ((BiologicalEntity)element).getConstraint()!=null && posKnowledgeBase.isAdverb(constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint)){
					&& !constraint.isEmpty() && !constraint.contains(" ") && !constraint.contains(";") && posKnowledgeBase.isAdverb(constraint)){ //constraint is a single adverb

				//other advs, mirrors the process above
				String mod = constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint;
				//handle is_modifier characters and true characters
				boolean hasTrueCharacters = false;
				for(Element character: new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
					String isModifier = character.getAttributeValue("is_modifier");
					if(isModifier != null && isModifier.equals("true")) {
						//turn this character to structure constraint
						biologicalEntity.setAttribute("constraint", (constraint + " " + character.getValue()).trim());
						character.detach();
					}else{
						//modify true characters
						hasTrueCharacters = true;
						String modifier = character.getAttributeValue("modifier"); 
						modifier = modifier ==null ? "": modifier;
						modifier = mod + " " + modifier;
						character.setAttribute("modifier", modifier.trim());
					}
				}

				//negate relations
				boolean modifiedRelation = false;
				List<Element> relations = this.getRelationsInvolve(biologicalEntity, document);
				for(Element relation: relations){
					modifiedRelation = true;
					String modifier = relation.getAttributeValue("modifier");
					modifier = modifier == null ? "": modifier;
					modifier = mod + " " + modifier;
					relation.setAttribute("modifier", modifier.trim());
				}

				//no relation and no true character, set count to 0
				if(!modifiedRelation && !hasTrueCharacters){
					Element count = new Element("character");
					count.setAttribute("name", "count");
					count.setAttribute("value", "present");
					count.setAttribute("modifier", mod);
					biologicalEntity.addContent(count);
				}

				constraint = biologicalEntity.getAttributeValue("constraint");
				//remove no|not|never from the structure constraint
				constraint = constraint.replaceFirst("^"+mod+"\\b", "");
				biologicalEntity.setAttribute("constraint", constraint.trim());
			}
		}
	}
	
}
