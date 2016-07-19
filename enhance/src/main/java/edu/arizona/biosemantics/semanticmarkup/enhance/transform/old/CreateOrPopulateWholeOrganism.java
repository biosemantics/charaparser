package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Create or populate whole organism with biological entities characters that match a set of target names and match a certain pattern (e.g. 'simple', 
 * not involved as a destination in a relation, ...) 
 * Useful targets are for example: lifestyles/growth_form or duration
 */
public class CreateOrPopulateWholeOrganism extends AbstractTransformer {

	private Set<String> targets;
	private String category;

	public CreateOrPopulateWholeOrganism(Set<String> targets, String category) {
		this.targets = targets;
		this.category = category;
	}
	
	@Override
	public void transform(Document document) {
		for(Element statement : new ArrayList<Element>(this.statementXpath.evaluate(document))) {
			Element wholeOrganism = getWholeOrganism(statement);
	
			for(Element biologicalEntity : new ArrayList<Element>(statement.getChildren("biological_entity"))) {
				String constraint = biologicalEntity.getAttributeValue("constraint");
				String name = biologicalEntity.getAttributeValue("name");
				name = ((constraint == null ? "" : constraint + " ") + name).trim();
				
				boolean isSimpleStructure = isSimpleStructure(biologicalEntity);
				if(targets.contains(name) && 
						!isToOrgan(biologicalEntity, document) && 
						!isConstraintOrgan(biologicalEntity, document)) {		
					
					if(wholeOrganism != null && isSimpleStructure) {
						List<Element> characters = new ArrayList<Element>(biologicalEntity.getChildren("character"));
						for(Element character : characters)
							character.detach();
						wholeOrganism.addContent(characters);
						modifyWholeOrganism(document, biologicalEntity, wholeOrganism, name);
						biologicalEntity.detach();
					} else if(wholeOrganism == null || !isSimpleStructure) {
						wholeOrganism = biologicalEntity;
						modifyWholeOrganism(document, biologicalEntity, wholeOrganism, name);
					}
				}
			}	
		}
	}
	
	private void modifyWholeOrganism(Document document, Element biologicalEntity, Element wholeOrganism, String name) {
		wholeOrganism.setAttribute("name", "whole_organism");
		wholeOrganism.setAttribute("name_original", "");
		wholeOrganism.setAttribute("type", "structure");
		
		Element character = new Element("character");
		character.setAttribute("name", category);
		character.setAttribute("value", name);
		wholeOrganism.addContent(character);
		updateFromStructureForRelations(biologicalEntity, wholeOrganism, document);
	}

	private Element getWholeOrganism(Element statement) {
		Element result = null;
		for(Element biologicalEntity : new ArrayList<Element>(statement.getChildren("biological_entity"))) {
			if(biologicalEntity.getAttributeValue("name").equals("whole_organism")) {
				result = biologicalEntity;
				break;
			}
		}
		return result;
	}

	private void updateFromStructureForRelations(Element biologicalEntity, Element wholeOrganism, Document document) {
		for(Element relation : new ArrayList<Element>(this.getFromRelations(biologicalEntity, document))) {
			relation.setAttribute("from", wholeOrganism.getAttributeValue("id"));
		}
	}	
	
	/**
	 * if the structue is used in a character constraint
	 * @param structure
	 * @param xml
	 * @return
	 */
	private boolean isConstraintOrgan(Element biologicalEntity, Document document) {
		String oid = biologicalEntity.getAttributeValue("id");
		for(Element entity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			for(Element character : new ArrayList<Element>(entity.getChildren("character"))) {
				String constraintId = character.getAttributeValue("constraintid");
				if(constraintId != null && constraintId.matches(".*?\\b" + oid + "\\b.*")) 
					return true;
			}
		}
		return false;
	}

	private boolean isToOrgan(Element biologicalEntity, Document document) {
		this.getToRelations(biologicalEntity, document);
		return !this.getToRelations(biologicalEntity, document).isEmpty();
	}
	
	private boolean isSimpleStructure(Element biologicalEntity) {
		String geographicalConstraint = biologicalEntity.getAttributeValue("geographical_constraint");
		String inBrackets = biologicalEntity.getAttributeValue("in_brackets");
		String notes = biologicalEntity.getAttributeValue("notes");
		String parallelismConstraint = biologicalEntity.getAttributeValue("parallelism_constraint");
		String provenance = biologicalEntity.getAttributeValue("provenance");
		String taxonConstraint = biologicalEntity.getAttributeValue("taxon_constraint");
		
		String complex = (geographicalConstraint == null? "" : geographicalConstraint) + 
				(inBrackets == null ? "" : inBrackets)+
				(notes == null? "" : notes)+
				(parallelismConstraint == null ? "" : parallelismConstraint)+
				(provenance == null ? "" : provenance)+
				(taxonConstraint == null ? "" : taxonConstraint);
		return complex.trim().length()==0;
	}
}
