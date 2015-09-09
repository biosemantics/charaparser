package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class WholeOrganismTransformer extends AbstractTransformer {

	private IGlossary glossary;

	public WholeOrganismTransformer(IGlossary glossary) {
		this.glossary = glossary;
	}
	
	/**
	 * some description paragraphs start to name the taxon the organism belongs to. 
	 * e.g. a description about 'Persian cats' starts with 'cats with long hairs and ...'
	 * the markup of such usage of taxon name is normalized to whole_organism here
	 * @param result
	 */
	private void taxonName2WholeOrganism(Document document) {
		for (Element statement : this.statementXpath.evaluate(document)) {
			List<Element> biologicalEntities = statement.getChildren("biological_entity");
			if(!biologicalEntities.isEmpty()) {
				Element firstElement = biologicalEntities.get(0);
				String type = firstElement.getAttributeValue("type");
				String constraint = firstElement.getAttributeValue("constraint");
				if (type != null && type.equals("taxon_name") && (
						constraint == null || constraint.isEmpty())) {
					firstElement.setAttribute("name", "whole_organism");
					firstElement.setAttribute("name_original", "");
					firstElement.setAttribute("type", "structure");
				}
			}
		}		
	}
	
	private void createWholeOrganismDescription(Document document, Set<String> targets, String category) {
		for(Element statement : new ArrayList<Element>(this.statementXpath.evaluate(document))) {
			Element wholeOrganism = new Element("biological_entity");
			boolean exist = false;
			for(Element biologicalEntity : new ArrayList<Element>(statement.getChildren("biological_entity"))) {
				if(biologicalEntity.getAttributeValue("name").equals("whole_organism")) {
					wholeOrganism = biologicalEntity;
					exist = true;
					break;
				}
			}
	
			boolean modifiedWholeOrganism = false;
			for(Element biologicalEntity : new ArrayList<Element>(statement.getChildren("biological_entity"))) {
				String constraint = biologicalEntity.getAttributeValue("constraint");
				String name = biologicalEntity.getAttributeValue("name");
				name = ((constraint == null ? "" : constraint + " ") + name).trim();
				boolean isSimpleStructure = isSimpleStructure(biologicalEntity);
				if(targets.contains(name) && !isToOrgan(biologicalEntity, document) && !isConstraintOrgan(biologicalEntity, document)) {		
					//wholeOrganism.appendAlterName(structure.getAlterName());
					//wholeOrganism.appendConstraint(structure.getConstraint());
					//wholeOrganism.appendConstraintId(structure.getConstraintId());
					//wholeOrganism.appendGeographicalConstraint(structure.getGeographicalConstraint());
					//wholeOrganism.appendId(structure.getId());
					//wholeOrganism.appendInBrackets(structure.getInBrackets());
					//wholeOrganism.appendNotes(structure.getNotes());
					//wholeOrganism.appendOntologyId(structure.getOntologyId());
					//wholeOrganism.appendParallelismConstraint(structure.getParallelismConstraint());
					//wholeOrganism.appendProvenance(structure.getProvenance());
					//wholeOrganism.appendTaxonConstraint(structure.getTaxonConstraint());
					if(exist && isSimpleStructure){
						wholeOrganism.addContent(biologicalEntity.getChildren("character"));
					}else if(!exist){
						wholeOrganism = biologicalEntity;
						exist = true;
					}else if(!isSimpleStructure){
						//in-place update of structure to whole_organism
						biologicalEntity.setAttribute("name", "whole_organism");
						biologicalEntity.setAttribute("name_original", "");
						biologicalEntity.setAttribute("type", "structure");
						Element character = new Element("character");
						character.setAttribute("name", category);
						character.setAttribute("value", name);
						biologicalEntity.addContent(character);
						continue;
					}
					wholeOrganism.setAttribute("name", "whole_organism");
					wholeOrganism.setAttribute("name_original", "");
					wholeOrganism.setAttribute("type", "structure");
					
					Element character = new Element("character");
					character.setAttribute("name", category);
					character.setAttribute("value", name);
					wholeOrganism.addContent(character);
					modifiedWholeOrganism = true;
					updateFromStructureForRelations(biologicalEntity, wholeOrganism, document);
					biologicalEntity.detach();
				}
			}	
	
			if(modifiedWholeOrganism)
				statement.addContent(wholeOrganism);
		}
	}
	
	private void updateFromStructureForRelations(Element biologicalEntity, Element wholeOrganism, Document document) {
		for(Element relation : new ArrayList<Element>(this.getFromRelations(biologicalEntity, document))) {
			relation.setAttribute("from", wholeOrganism.getAttributeValue("id"));
		}
	}
	
	//if unknown_subject has no characters and no relations, remove them.
	private void removeOrphenedUnknownElements(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null && name.equals("whole_organism") && biologicalEntity.getChildren("character").isEmpty()) {
				//String id = ((Structure)element).getId();
				if(getRelationsInvolve(biologicalEntity, document).isEmpty()) 
					biologicalEntity.detach();
			}
		}
		/*		List<Element> unknowns = unknownsubject.selectNodes(this.statement);
				for(Element unknown : unknowns){
					if(unknown.getChildren().size()==0){ 
						String id = unknown.getAttributeValue("id");
						List<Element> relations = XPath.selectNodes(this.statement, ".//relation[@from='"+id+"']|.//relation[@to='"+id+"']");
						if(relations.size()==0) unknown.detach();
					}else{ //add name_original
						unknown.setAttribute("name_original", ""); //name_original = "" as it was not in the original text
					}
				}	
		 */		

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

	@Override
	public void transform(Document document) {
		taxonName2WholeOrganism(document);
		
		Set<String> lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		Set<String> durations = glossary.getWordsInCategory("duration");
		
		createWholeOrganismDescription(document, lifeStyles, "growth_form");
		createWholeOrganismDescription(document, durations, "duration");
		
		removeOrphenedUnknownElements(document);
	}
}
