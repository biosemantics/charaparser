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

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 * Perform standarizations that does not need an ontology
 * such as presence = absent => count = 0
 */
public class NonOntologyBasedStandardizer {
	private Set<String> lifeStyles;
	private String sentence;
	private ProcessingContext processingContext;
	
	public NonOntologyBasedStandardizer(IGlossary glossary, String sentence, ProcessingContext processingContext){
		lifeStyles = glossary.getWords("life_style");
		this.sentence = sentence;
		this.processingContext = processingContext;
	}
	
	public void standardize(LinkedList<Element>result){
		createWholeOrganismDescription(result); 
		//createMayBeSameRelations(result, processingContext);  //not sure we need this relation.
		removeOrphenedUnknownElements(result);
		normalizeNegatedOrgan(result, sentence);
		normalizeZeroCount(result);
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


	private void createWholeOrganismDescription(List<Element> result) {
		Structure wholeOrganism = new Structure();
		boolean exist = false;
		for(Element element : result) {
			if(element.isStructure() && ((Structure)element).getName().equals("whole_organism")) {
				wholeOrganism = (Structure)element;
				exist = true;
				break;
			}
		}
		
		boolean modifiedWholeOrganism = false;
		Iterator<Element> resultIterator = result.iterator();
		while(resultIterator.hasNext()) {
			Element element = resultIterator.next();
			if(element.isStructure()) {
				Structure structure = (Structure)element;
				String name = (structure.getConstraint()+" "+structure.getName()).trim();
				boolean isSimpleStructure = isSimpleStructure(structure);
				if(lifeStyles.contains(name) && !isToOrgan(structure) && !isConstraintOrgan(structure, result)) {		
					//wholeOrganism.appendAlterName(structure.getAlterName());
					//wholeOrganism.appendConstraint(structure.getConstraint());
					//wholeOrganism.appendConstraintId(structure.getConstraintId());
					//wholeOrganism.appendGeographicalConstraint(structure.getGeographicalConstraint());
					//wholeOrganism.appendId(structure.getId());
					//wholeOrganism.appendInBracket(structure.getInBracket());
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
						Character character = new Character();
						character.setName("life_style");
						character.setValue(name);
						structure.addCharacter(character);
						continue;
					}
					wholeOrganism.setName("whole_organism");
					Character character = new Character();
					character.setName("life_style");
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


	private void updateFromStructureForRelations(Structure structure, Structure wholeOrganism) {
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
	private boolean isConstraintOrgan(Structure structure, List<Element> xml) {
		String oid = structure.getId();
		for(Element element: xml){
			if(element.isStructure()){
				LinkedHashSet<Character> chars = ((Structure)element).getCharacters();
				for(Character c: chars){
					if(c.getConstraintId().matches(".*?\\b"+oid+"\\b.*")) return true;
				}				
			}
		}
		return false;
	}

	private boolean isToOrgan(Structure structure) {
		return structure.getToRelations().size()>0;
	}

	private boolean isSimpleStructure(Structure structure) {
		String complex = structure.getGeographicalConstraint()+structure.getInBracket()+structure.getInBrackets()+structure.getNotes()+structure.getParallelismConstraint()+
				structure.getProvenance()+structure.getTaxonConstraint();
		return complex.trim().length()==0;
	}

	/**
	 * 
	 * 
	 * <statement id="1_1.txtp4.txt-0">
            <text>No winged queens are known.</text>
            <structure id="o42" name="queen">
              <character name="presence" value="no" />
              <character name="architecture" value="winged" />
            </structure>
          </statement>
	 * 
	 * =>
	 * 
	 * <statement id="1_1.txtp4.txt-0">
            <text>No winged queens are known.</text>
            <structure id="o42" name="queen">
               <character name="architecture" value="winged" modifer="not" />
            </structure>
          </statement>
	 * 
	 * no organ present => organ count = "0"
	 * no red organ => organ red modifier = "not"
	 * a has no organ => a has organ negation = "true"
	 * no organ touched a => organ touched a negation = "true"
	 * 
	 * 
	 * 1. if a relation is involved, negate the relation (not the character)
	 * 2. if not, negate the characters of the structure
	 * 3. if no characters, add count=0
	 * 
	 * run this before normalizeZeroCount.
	 * 
	 * @param xml
	 */
	private void normalizeNegatedOrgan(List<Element> result, String sentence) {
		List<Character> remove = new ArrayList<Character>();
		for(Element element: result){
			if(element.isStructure()){
				String structurename = ((Structure)element).getName();
				String constraint = ((Structure)element).getConstraint();
				if(constraint!=null) structurename = constraint +" "+ structurename;
				LinkedHashSet<Character> characters = ((Structure)element).getCharacters();
				int i = 0;
				for(Character character: characters){
					if(i==0 && character.getName()!=null && character.getName().compareTo("presence")==0 && character.getValue()!=null
							&& character.getValue().compareTo("no") ==0 && sentence.toLowerCase().matches(".*?\\bno\\b.*?"+structurename+".*")){//Hong TODO the matching is shaky
						//normalize
						boolean negatedRelation = false;
						boolean negatedCharacter = false;
						 //1. add negation = true for relations
						List<Relation> relations = this.getRelationsInvolve(((Structure)element), result);
						for(Relation relation: relations){
							negatedRelation = true;
							if(relation.getNegation() !=null &&
									relation.getNegation().compareTo("true") == 0) relation.setNegation("false");
							else relation.setNegation("true");
						
						}
						if(!negatedRelation){
							if(characters.size()>1){ 
								//has characters other than presence="no"
								//2. negate character
								int j = 0;
								for(Character chara: characters){
									if(j>0){
										chara.setModifier("not");
										negatedCharacter = true;
									}
									j++;
								}
								if(negatedCharacter) remove.add(character);
							}
							if(!negatedCharacter){
								//3. structure count = 0
								Character count = new Character();
								count.setName("count");
								count.setValue("0");
								((Structure)element).addCharacter(count);
							}
						}
						
					}else{
						break;
					}
					i++;
				}
				characters.removeAll(remove);
			}
			
		}
		
	}


	/**
	 * 	nomarlization count
     *  count = "none" =>count = 0; 
     *  count = "absent" =>count = 0;
     *  count = "present", modifier = "no|not|never" =>count = 0;
	 * @param xml
	 */

	private void normalizeZeroCount(List<Element> xml) {
		for(Element element: xml){
			if(element.isStructure()){
				LinkedHashSet<Character> characters = ((Structure)element).getCharacters();
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
			if(element.isStructure() && ((Structure)element).getName()!=null &&
					((Structure)element).getName().compareTo("whole_organism") == 0 && 
					((Structure)element).getCharacters()!=null && ((Structure)element).getCharacters().size()==0){
				//String id = ((Structure)element).getId();
				List<Relation> relations = getRelationsInvolve((Structure)element, xml);
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


	private List<Relation> getRelationsInvolve(Structure struct, List<Element> xml) {
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
