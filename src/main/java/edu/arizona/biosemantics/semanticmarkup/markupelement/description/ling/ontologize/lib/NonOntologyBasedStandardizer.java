/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 * Perform standarizations of a description paragraph without using an ontology
 * such as presence = absent => count = 0
 *        
 */
public class NonOntologyBasedStandardizer {
	private Set<String> lifeStyles;
	private Set<String> durations;
	//private String sentence;
	private ProcessingContext processingContext;
	private IPOSKnowledgeBase posKnowledgeBase;

	public NonOntologyBasedStandardizer(IGlossary glossary, String sentence, ProcessingContext processingContext, @Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase){
		lifeStyles = glossary.getWords("life_style");
		lifeStyles.addAll(glossary.getWords("growth_form"));
		durations = glossary.getWords("duration");
		//this.sentence = sentence;
		this.processingContext = processingContext;
		this.posKnowledgeBase = posKnowledgeBase;
	}

	public void standardize(LinkedList<Element>result){
		if(result.isEmpty()) return;
		taxonName2WholeOrganism(result);
		createWholeOrganismDescription(result, lifeStyles, "growth_form");
		createWholeOrganismDescription(result, durations, "duration");
		//createMayBeSameRelations(result, processingContext);  //not sure we need this relation.
		removeOrphenedUnknownElements(result);
		noOrgan2AdvConstraintedOrgan(result);
		normalizeAdvConstraintedOrgan(result);	
		normalizeZeroCount(result);
		removeCircularCharacterConstraint(result);
		character2structureContraint(result);
	}


	/**
	 * if character name  = entity structural constraint type 
	 * and is_modifier = true
	 * then make the character a structure constraint
	 * @param result
	 */
	private void character2structureContraint(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				String oid = ((BiologicalEntity)element).getId();
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				List<Character> removes = new ArrayList<Character>();
				for(Character c: chars){
					if(c.getIsModifier()!=null && c.getIsModifier().compareTo("true")==0 && c.getName()!=null &&
							c.getName().matches(".*?(^|_or_)("+ElementRelationGroup.entityStructuralConstraintElements+")(_or_|$).*")){
						if(c.getValue()!=null){
							((BiologicalEntity) element).appendConstraint(c.getValue());
							removes.add(c);
						}
						
					}
				}	
				chars.removeAll(removes);
				/*for(Character c: removes){
					((BiologicalEntity)element).removeElementRecursively(c);
				}*/
			}
		}
		
	}

	/**
	 * some description paragraphs start to name the taxon the organism belongs to. 
	 * e.g. a description about 'Persian cats' starts with 'cats with long hairs and ...'
	 * the markup of such usage of taxon name is normalized to whole_organism here
	 * @param result
	 */
	private void taxonName2WholeOrganism(LinkedList<Element> result) {
		Element firstElement =result.get(0);
		if(firstElement.isStructure()){
			String type = ((BiologicalEntity)firstElement).getType();
			if(type!=null && type.compareTo("taxon_name")==0 && ( 
					((BiologicalEntity)firstElement).getConstraint()==null ||((BiologicalEntity)firstElement).getConstraint().isEmpty())){
				((BiologicalEntity)firstElement).setName("whole_organism");
				((BiologicalEntity)firstElement).setNameOriginal("");
				((BiologicalEntity)firstElement).setType("structure");
			}
		}
	}
	/**
	 * if a character constraint refers to the same structure the character belongs to, remove the constraint
	 * @param result
	 */
	private void removeCircularCharacterConstraint(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				String oid = ((BiologicalEntity)element).getId();
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				for(Character c: chars){
					if(c.getConstraintId()!=null && c.getConstraintId().matches(".*?\\b"+oid+"\\b.*")){
						c.setConstraint(null);
						c.setConstraintId(null);
					}
				}				
			}
		}

	}

	/*private void createMayBeSameRelations(List<Element> result, ProcessingContext processingContext) {
		HashMap<String, Set<String>> names = new HashMap<String, Set<String>>();
		for (Element element : result) {
			if (element.isStructure()) {
				Structure structure = (Structure)element;
				String name = structure.getName();

				//if (element.containsAttribute("constraintType"))
				//	name = element.getCongetAttribute("constraintType") + " " + name;
				//if (element.containsAttribute("constraintParentOrgan"))
				//	name = element.getAttribute("constraintParentOrgan") + " " + name;
				//if (element.containsAttribute("constraint"))
				//	name = element.getAttribute("constraint") + " " + name;

				if (structure.getConstraint() != null && !structure.getConstraint().isEmpty())
					name = structure.getConstraint() + " " + name;

				String id = structure.getId();
				if(!names.containsKey(name)) 
					names.put(name, new HashSet<String>());
				names.get(name).add(id);
			}
		}

		for(Entry<String, Set<String>> nameEntry : names.entrySet()) {
			Set<String> ids = nameEntry.getValue();
			if(ids.size() > 1) {
				Iterator<String> idIterator = ids.iterator();
				while(idIterator.hasNext()) {
					String idA = idIterator.next();
					for(String idB : ids) {
						if(!idA.equals(idB)) {
							Relation relationElement = new Relation();
							relationElement.setName("may_be_the_same");
							relationElement.setFrom(idA);
							relationElement.setTo(idB);
							relationElement.setNegation(String.valueOf(false));
							relationElement.setToStructure(structure);
							relationElement.setFromStructure(structure);
							relationElement.setId("r" + String.valueOf(processingContext.fetchAndIncrementRelationId(relationElement)));	
						}
					}
					idIterator.remove();
				}
			}
		}
	}*/


	private void createWholeOrganismDescription(List<Element> result, Set<String> targets, String category) {
		BiologicalEntity wholeOrganism = new BiologicalEntity();
		boolean exist = false;
		for(Element element : result) {
			if(element.isStructure() && ((BiologicalEntity)element).getName().equals("whole_organism")) {
				wholeOrganism = (BiologicalEntity)element;
				exist = true;
				break;
			}
		}

		boolean modifiedWholeOrganism = false;
		Iterator<Element> resultIterator = result.iterator();
		while(resultIterator.hasNext()) {
			Element element = resultIterator.next();
			if(element.isStructure()) {
				BiologicalEntity structure = (BiologicalEntity)element;
				String name = ((structure.getConstraint()==null? "": structure.getConstraint()+" ")+structure.getName()).trim();
				boolean isSimpleStructure = isSimpleStructure(structure);
				if(targets.contains(name) && !isToOrgan(structure) && !isConstraintOrgan(structure, result)) {		
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
						LinkedHashSet<Character> characters = structure.getCharacters();
						wholeOrganism.addCharacters(characters);
					}else if(!exist){
						wholeOrganism = structure;
						exist = true;
					}else if(!isSimpleStructure){
						//in-place update of structure to whole_organism
						structure.setName("whole_organism");
						structure.setNameOriginal("");
						structure.setType("structure");
						Character character = new Character();
						character.setName(category);
						character.setValue(name);
						structure.addCharacter(character);
						continue;
					}
					wholeOrganism.setName("whole_organism");
					wholeOrganism.setNameOriginal("");
					wholeOrganism.setType("structure");
					Character character = new Character();
					character.setName(category);
					character.setValue(name);
					wholeOrganism.addCharacter(character);
					modifiedWholeOrganism = true;
					updateFromStructureForRelations(structure, wholeOrganism);
					resultIterator.remove();
				}
			}	
		}

		if(modifiedWholeOrganism)
			result.add(wholeOrganism);
	}


	private void updateFromStructureForRelations(BiologicalEntity structure, BiologicalEntity wholeOrganism) {
		LinkedHashSet<Relation> relations = structure.getFromRelations();
		for(Relation r: relations){
			r.setFromStructure(wholeOrganism);
		}
	}

	/**
	 * if the structue is used in a character constraint
	 * @param structure
	 * @param xml
	 * @return
	 */
	private boolean isConstraintOrgan(BiologicalEntity structure, List<Element> xml) {
		String oid = structure.getId();
		for(Element element: xml){
			if(element.isStructure()){
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				for(Character c: chars){
					if(c.getConstraintId()!=null && c.getConstraintId().matches(".*?\\b"+oid+"\\b.*")) return true;
				}				
			}
		}
		return false;
	}

	private boolean isToOrgan(BiologicalEntity structure) {
		return structure.getToRelations().size()>0;
	}

	private boolean isSimpleStructure(BiologicalEntity structure) {
		String complex = (structure.getGeographicalConstraint()==null? "":structure.getGeographicalConstraint()) + 
				(structure.getInBrackets()==null? "":structure.getInBrackets())+
				(structure.getNotes()==null? "":structure.getNotes())+
				(structure.getParallelismConstraint()==null? "":structure.getParallelismConstraint())+
				(structure.getProvenance()==null? "":structure.getProvenance())+
				(structure.getTaxonConstraint()==null? "":structure.getTaxonConstraint());
		return complex.trim().length()==0;
	}

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
	private void noOrgan2AdvConstraintedOrgan(List<Element> result) {
		List<Character> remove = new ArrayList<Character>();
		for(Element element: result){
			if(element.isStructure()){
				LinkedHashSet<Character> characters = ((BiologicalEntity)element).getCharacters();
				int i = 0;
				for(Character character: characters){
					if(i==0 && character.getName()!=null && character.getName().compareTo("count")==0 && character.getValue()!=null
							&& character.getValue().compareTo("no") ==0 && character.getIsModifier()!=null && character.getIsModifier().compareTo("true") == 0){
						String constraint = ((BiologicalEntity)element).getConstraint()==null? "" :  ((BiologicalEntity)element).getConstraint();
						((BiologicalEntity)element).setConstraint(("no "+constraint).trim());
						remove.add(character);
					}
				}
				characters.removeAll(remove);
			}
		}
	}

	/**
	 * 
	 *1.  <structure constraint="never" id="o3" name="sterraster" name_original="sterrasters" notes="structure" /> => no character: count=0, with character: negate modifier for all non-is_modifier characters:add is_modifier character to structure constraint
	 *2.  <structure constraint="usually" id="o3" name="sterraster" name_original="sterrasters" notes="structure" /> => no character: count=present, modifier=usually, with character: add usually to all characters
	 *3. relation: Never microscleres touches euasters
	 *<structure name="microsclere" constraint="never" id="o1" notes="structure" name_original="microscleres"/>
<structure name="euaster" id="o2" notes="structure" name_original="euasters"/>
<relation name="touches" from="o1" id="r0" negation="false" to="o2"/>
	 *
	 *turn is_modifier characters to structure constraint.
	 *negate relation and all true characters (characters that are not is_modifier characters).
	 *if none of the above (relation/true characters) exist, set count=0;
	 *
	 * run that this before normalizeZeroCount.
	 * @param result
	 */

	private void normalizeAdvConstraintedOrgan(LinkedList<Element> result) {
		ArrayList<Character> remove = new ArrayList<Character>();
		for(Element element: result){
			if(element.isStructure()){
				String constraint = ((BiologicalEntity)element).getConstraint()==null? "":  ((BiologicalEntity)element).getConstraint();
				if(((BiologicalEntity)element).getType()!=null && ((BiologicalEntity)element).getType().compareTo("structure")==0
						&& ((BiologicalEntity)element).getConstraint()!=null && ((BiologicalEntity)element).getConstraint().matches("^(no|not|never)\\b.*")){
					//adv is negation
					//handle is_modifier characters and true characters
					boolean hasTrueCharacters = false;
					LinkedHashSet<Character> characters = ((BiologicalEntity)element).getCharacters();
					for(Character character: characters){
						if(character.getIsModifier()!=null && character.getIsModifier().compareTo("true")==0){
							//turn this character to structure constraint			
							constraint = ((BiologicalEntity)element).getConstraint()==null? "":  ((BiologicalEntity)element).getConstraint();
							((BiologicalEntity)element).setConstraint((constraint+ " "+character.getValue()).trim());
							remove.add(character);
						}else{
							//negate true characters
							hasTrueCharacters = true;
							String modifier = character.getModifier();
							if(modifier==null){
								character.setModifier("not");
							}else if(modifier.matches(".*\\bnot\\b.*")){//double negation 
								modifier = modifier.replaceFirst("\\bnot\\b", "");
								character.setModifier(modifier.replaceAll("\\s+", " ").trim());
							}else{
								modifier ="not "+modifier;
								character.setModifier(modifier.trim());
							}
						}
					}
					//negate relations
					boolean negatedRelation = false;
					List<Relation> relations = this.getRelationsInvolve(((BiologicalEntity)element), result);
					for(Relation relation: relations){
						negatedRelation = true;
						if(relation.getNegation() !=null &&
								relation.getNegation().compareTo("true") == 0) relation.setNegation("false");
						else relation.setNegation("true");
					}
					
					//no relation and no true character, set count to 0
					if(!negatedRelation && !hasTrueCharacters){
						Character count = new Character();
						count.setName("count");
						count.setValue("0");
						((BiologicalEntity) element).addCharacter(count);
					}
					
					//remove unneeded stuff
					characters.removeAll(remove);
					//remove no|not|never from the structure constraint
					constraint = ((BiologicalEntity)element).getConstraint()==null? "":  ((BiologicalEntity)element).getConstraint().replaceFirst("^no|not|never\\b", "");
					((BiologicalEntity)element).setConstraint(constraint.trim());
				}else if(((BiologicalEntity)element).getType()!=null && ((BiologicalEntity)element).getType().compareTo("structure")==0 
						&& ((BiologicalEntity)element).getConstraint()!=null && posKnowledgeBase.isAdverb(constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint)){
					//other advs, mirrors the process above
					String mod = constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint;
					//handle is_modifier characters and true characters
					boolean hasTrueCharacters = false;
					LinkedHashSet<Character> characters = ((BiologicalEntity)element).getCharacters();
					for(Character character: characters){
						if(character.getIsModifier()!=null && character.getIsModifier().compareTo("true")==0){
							//turn this character to structure constraint			
							constraint = ((BiologicalEntity)element).getConstraint()==null? "":  ((BiologicalEntity)element).getConstraint();
							((BiologicalEntity)element).setConstraint((constraint+ " "+character.getValue()).trim());
							remove.add(character);
						}else{
							//modify true characters
							hasTrueCharacters = true;
							String modifier = character.getModifier()==null? "": character.getModifier();
							modifier =mod+" "+modifier;
							character.setModifier(modifier.trim());
						}
					}
					
					//negate relations
					boolean modifiedRelation = false;
					List<Relation> relations = this.getRelationsInvolve(((BiologicalEntity)element), result);
					for(Relation relation: relations){
						modifiedRelation = true;
						String modifier = relation.getModifier()==null? "": relation.getModifier();
						modifier =mod+" "+modifier;
						relation.setModifier(modifier.trim());
					}
					
					//no relation and no true character, set count to 0
					if(!modifiedRelation && !hasTrueCharacters){
						Character count = new Character();
						count.setName("count");
						count.setValue("present");
						count.setModifier(mod);
						((BiologicalEntity) element).addCharacter(count);
					}
					
					//remove unneeded stuff
					characters.removeAll(remove);
					//remove no|not|never from the structure constraint
					constraint = ((BiologicalEntity)element).getConstraint()==null? "":  ((BiologicalEntity)element).getConstraint().replaceFirst("^"+mod+"\\b", "");
					((BiologicalEntity)element).setConstraint(constraint.trim());
				}
			}
		}
	}


	/**
	 * 	nomarlization count
	 *  count = "none" =>count = 0; 
	 *  count = "absent" =>count = 0;
	 *  count = "present", modifier = "no|not|never" =>count = 0;
	 *  
	 * @param xml
	 */

	private void normalizeZeroCount(List<Element> xml) {
		for(Element element: xml){
			if(element.isStructure()){
				LinkedHashSet<Character> characters = ((BiologicalEntity)element).getCharacters();
				for(Character character: characters){
					if(character.getName()!=null && character.getName().compareTo("count")==0){
						if(character.getValue()!=null){
							if(character.getValue().compareTo("none")==0) character.setValue("0"); 
							if(character.getValue().compareTo("absent") == 0 
									&& (character.getModifier()==null || !character.getModifier().matches("no|not|never"))) 
								character.setValue("0"); 
							if(character.getValue().compareTo("present") == 0 
									&& character.getModifier()!=null && character.getModifier().matches("no|not|never")){ 
								character.setValue("0");
								character.setModifier("");
							}
						}
					}	
				}
			}
		}



		/*List<Element> es = path1.selectNodes(this.statement);
        for (Element e : es) {
            e.setAttribute("value", "0");
        }*/

	}


	//if unknown_subject has no characters and no relations, remove them.
	private void removeOrphenedUnknownElements(List<Element> xml) {
		List<Element> remove = new ArrayList<Element> (); 
		for(Element element: xml){
			if(element.isStructure() && ((BiologicalEntity)element).getName()!=null &&
					((BiologicalEntity)element).getName().compareTo("whole_organism") == 0 && 
					((BiologicalEntity)element).getCharacters()!=null && ((BiologicalEntity)element).getCharacters().size()==0){
				//String id = ((Structure)element).getId();
				List<Relation> relations = getRelationsInvolve((BiologicalEntity)element, xml);
				if(relations.size()==0) remove.add(element);				
			}
		}
		xml.removeAll(remove);
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


	private List<Relation> getRelationsInvolve(BiologicalEntity struct, List<Element> xml) {
		List<Relation> relations = new ArrayList<Relation>();
		for(Element element: xml){
			if(element.isRelation() && ((((Relation)element).getFromStructure()!=null && ((Relation)element).getFromStructure().equals(struct)) || 
					(((Relation)element).getToStructure()!=null && ((Relation)element).getToStructure().equals(struct)))){
				relations.add((Relation)element);				
			}
		}
		return relations;
	}



}
