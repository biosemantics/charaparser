/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;
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
	private String sentence;
	private ProcessingContext processingContext;
	private IPOSKnowledgeBase posKnowledgeBase;
	private IInflector inflector;

	public NonOntologyBasedStandardizer(IGlossary glossary, IInflector inflector, String sentence, ProcessingContext processingContext, @Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase){
		lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		durations = glossary.getWordsInCategory("duration");
		this.sentence = sentence;
		this.processingContext = processingContext;
		this.posKnowledgeBase = posKnowledgeBase;
		this.inflector = inflector;
	}

	public void standardize(LinkedList<Element>result){
		if(result.isEmpty()) return;
		enumerateCompoundOrgan(result); //legs i-iii
		enumerateCompoundStates(result); // tibia/metatarsus:  1.43/1.27 mm
		checkAlternativeIDs(result); //before count
		taxonName2WholeOrganism(result);
		createWholeOrganismDescription(result, lifeStyles, "growth_form");
		createWholeOrganismDescription(result, durations, "duration");
		//createMayBeSameRelations(result, processingContext);  //not sure we need this relation.
		removeOrphenedUnknownElements(result);
		noOrgan2AdvConstraintedOrgan(result);
		normalizeZeroCount(result);
		removeCircularCharacterConstraint(result);
		character2structureContraint(result);//is_modifier => constraint
		renameCharacter(result, "count", "quantity");
		renameCharacter(result, "atypical_count", "atypical_quantity");
		renameCharacter(result, "color", "coloration");
		quantityVsPresence(result); //after count => quantity
		phraseUpConstraints(result); //put constraints in the order as appeared in the original text, should be among the last normalization steps
		normalizeAdvConstraintedOrgan(result);	//after phraseUpConstraints
	}
	
	/**
	 * 
	<statement id="d0_s2">
	<text>Length of tibia/metatarsus: leg-I, 1.43/1.27 mm;</text>
	<biological_entity constraint="leg-1" id="o3" name="tibia/metatarsu" name_original="tibia/metatarsus" type="structure">
	<character name="length" unit="mm" value="1.43/1.27" />
	</biological_entity>
	</statement>
	 * @param result
	 */

	private void enumerateCompoundStates(LinkedList<Element> result) {
		ArrayList<Element> toberemoved = new ArrayList<Element>();
		for(int i = 0; i < result.size(); i++){
			Element element = result.get(i);
			if(element.isStructure()){
				boolean isTarget = true;
				BiologicalEntity entity = (BiologicalEntity) element;
				String[] entityNames = null;
				ArrayList<String[]> characterValues = new ArrayList<String[]>();
				if(entity.getName().contains("/")){ //add all character values also contain /
					entityNames = entity.getName().split("\\s*/\\s*");
					if(entity.getCharacters().isEmpty()) isTarget = false; //distal 1/2
					for(Character character: entity.getCharacters()){
						if(character.getValue().contains("/")){
							String [] cValues = character.getValue().split("\\s*/\\s*");
							characterValues.add(cValues);
							if(cValues.length != entityNames.length) isTarget = false;
						}else{
							isTarget = false;
						}
					}

				}else{
					isTarget= false;
				}

				if(isTarget){//enumerate
					toberemoved.add(entity);
					int p = result.indexOf(entity);
					ArrayList<String> newIds = new ArrayList<String>();
					for(int e = 0; e < entityNames.length; e++){
						BiologicalEntity be = entity.clone();
						String newId = entity.getId()+"_"+(e+1);
						be.setName(entityNames[e]);
						be.setId(newId);
						newIds.add(newId);
						int c = 0;
						for(Character character: be.getCharacters()){
							character.setValue(characterValues.get(c++)[e]);
						}
						result.add(p, be);
						i++;
					}

					//if the original element involved in any relations, individualize the relations
					//to
					LinkedHashSet<Relation> toRelations = entity.getToRelations();
					for(Relation relation: toRelations){
						int relationPosition = result.indexOf(relation);						
						for(String newId: newIds){
							Relation one = relation.clone();
							one.setTo(newId);
							result.add(relationPosition+1, one);
						}
						//result.remove(relationPosition);	
						toberemoved.add(relation);
					}
					//from
					LinkedHashSet<Relation> fromRelations = entity.getFromRelations();
					for(Relation relation: fromRelations){
						int relationPosition = result.indexOf(relation);						
						for(String newId: newIds){
							Relation one = relation.clone();
							one.setFrom(newId);
							result.add(relationPosition+1, one);
						}
						//result.remove(relationPosition);	
						toberemoved.add(relation);
					}
				}
			}
		}
		result.removeAll(toberemoved);
	}

	/**
	 * <biological_entity constraint="legs" id="o0" name="i-iii" name_original="i-iii" type="structure">
		<character constraint="than leg-iv" constraintid="o1" is_modifier="false" name="fragility" value="stronger" />
	   </biological_entity>
	 * @param result
	 */

	private void enumerateCompoundOrgan(LinkedList<Element> result) {
		ArrayList<Element> toberemoved = new ArrayList<Element>();
		for(int i = 0; i < result.size(); i++){
			Element element = result.get(i);
			if(element.isStructure()){
				BiologicalEntity entity = (BiologicalEntity) element;
				String name = entity.getNameOriginal();
				if(name.matches("(\\d+|[ivx]+)-(\\d+|[ivx]+)")){
					//int elementPosition = result.indexOf(element);
					ArrayList<String> list = getIndividuals (name);
					boolean added =false;
					int count = 1;
					ArrayList<String> newIds = new ArrayList<String>();
					for(String individual: list){
						BiologicalEntity one = entity.clone();
						//clone but update id
						((BiologicalEntity) one).setName(inflector.getSingular(individual));
						String newId = one.getId()+"_"+count;
						one.setId(newId);
						newIds.add(newId);
						count++;
						result.add(i+1, one);
						i++;
						added = true;
					}
					if(added){
						//remove the original biological entity element
						toberemoved.add(element);
						//result.remove(elementPosition);
					
					    //if the original element involved in any relations, individualize the relations
						//to
						LinkedHashSet<Relation> toRelations = entity.getToRelations();
						for(Relation relation: toRelations){
							int relationPosition = result.indexOf(relation);						
							for(String newId: newIds){
								Relation one = relation.clone();
								one.setTo(newId);
								result.add(relationPosition+1, one);
							}
							//result.remove(relationPosition);	
							toberemoved.add(relation);
						}
						//from
						LinkedHashSet<Relation> fromRelations = entity.getFromRelations();
						for(Relation relation: fromRelations){
							int relationPosition = result.indexOf(relation);						
							for(String newId: newIds){
								Relation one = relation.clone();
								one.setFrom(newId);
								result.add(relationPosition+1, one);
							}
							//result.remove(relationPosition);	
							toberemoved.add(relation);
						}
					
					
					}
					
				}
			}
		}
		result.removeAll(toberemoved);
	}

	private ArrayList<String> getIndividuals(String name) {
		String [] ends = name.split("-");
		ArrayList<String> individuals = new ArrayList<String>();
		String current = ends[0];
		String last = ends[ends.length-1];
		while(current.compareTo(last)!=0){
			individuals.add(current);
			current = nextRoman(current);
		}
		individuals.add(last);
		return individuals;
	}
	
	/**
	 * 
	 * @param roman <= XXXVIII (38)
	 * @return
	 */
	private String nextRoman(String roman){
		if(roman.endsWith("iv")){
			return roman.replaceFirst("iv$", "v");
		}else if(roman.endsWith("ix")){
			return roman.replaceFirst("ix$", "x");
		}else if(roman.endsWith("viii")){
			return roman.replaceFirst("viii$", "ix");
		}else if(roman.endsWith("iii")){
			return roman.replaceFirst("iii$", "iv");
		}else 
			return roman+"i";
	}

	/**
		<text>ovules 2-4, glabrous, lamina ovate 25-4 mm long, 15-25 mm wide, deeply pectinate, with 14-22 soft lateral spines 15-25 mm long, 2.5 mm wide, apical spine not distinct from lateral spines.</text>
		<biological_entity id="o1" name="ovule" name_original="ovules" type="structure">
		<character char_type="range_value" from="2" name="quantity" to="4" />
		<character is_modifier="false" name="pubescence" value="glabrous" />
		</biological_entity>
		<biological_entity id="o2" name="lamina" name_original="lamina" type="structure">
		<character is_modifier="false" name="shape" value="ovate" />
		<character char_type="range_value" from="25" from_unit="mm" name="length" to="4" to_unit="mm" />
		<character char_type="range_value" from="15" from_unit="mm" name="width" to="25" to_unit="mm" />
		<character is_modifier="false" modifier="deeply" name="shape" value="pectinate" />
	 ************
		<character name="width" notes="alterIDs:o3" unit="mm" value="2.5" />
	 ************
		</biological_entity>
		<biological_entity constraint="lateral" id="o3" name="spine" name_original="spines" type="structure">
		<character char_type="range_value" from="14" is_modifier="true" name="quantity" to="22" />
		<character is_modifier="true" name="pubescence_or_texture" value="soft" />
		<character char_type="range_value" from="15" from_unit="mm" name="length" to="25" to_unit="mm" />
		</biological_entity>
	 * 
	 * check characters with an alterIDs note, 
	 * If the current entity has the same kind of measurements (typical/atypical width, length, height, size with numerical values), 
	 * move the character to the entities indicated by the alterIDs. Any following charWAIs are moved too.
	 * 
	 * Otherwise, do nothing
	 * 
	 * @param result
	 */

	private void checkAlternativeIDs(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				LinkedHashSet<Character> characters = ((BiologicalEntity)element).getCharacters(); //in the order of parsing results?
				ArrayList<Character> withAlterIDs = new ArrayList<Character> ();
				for(Character character: characters){
					String notes = character.getNotes();
					if(notes !=null && notes.contains("alterIDs:")){
						withAlterIDs.add(character);
					}

				}

				boolean move = false;
				ArrayList<Character> removes = new ArrayList<Character>();
				for(Character charWAI: withAlterIDs){
					String notes = charWAI.getNotes();
					removeAlterIDsNotes(charWAI);
					String note = notes.substring(notes.indexOf("alterIDs:"));
					List<String> ids = Arrays.asList(note.replaceFirst("[;\\.].*", "").replaceFirst("alterIDs:", "").split("\\s+"));
					String charName = charWAI.getName(); //width, atypical_width
					if(move){
						removes.add(charWAI);
						moveCharacter2Structures(charWAI, ids, result);
					}
					else{
						for(Character character: characters){
							if(!character.equals(charWAI) && charName.compareTo(character.getName())==0 && charName.matches(".*?_?(count|length|width|size|height)")){
								//move charWAI to entities with the ids
								removes.add(charWAI);
								moveCharacter2Structures(charWAI, ids, result); //
								move = true;
								break;
							}
						}
					}
				}
				characters.removeAll(removes);
			}
		}

	}


	private void moveCharacter2Structures(Character charWAI, List<String> list, LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				if(list.contains(((BiologicalEntity) element).getId())){
					((BiologicalEntity) element).addCharacter(charWAI);
				}
			}
		}
	}

	private void removeAlterIDsNotes(Character charWAI) {
		if(charWAI.getNotes()!=null && charWAI.getNotes().contains("alterIDs:")){
			charWAI.setNotes(charWAI.getNotes().replaceFirst("\\balterIDs:.*?(;|\\.|$)", "")); //remove alterIDs note
			if(charWAI.getNotes()!=null && charWAI.getNotes().isEmpty()) charWAI.setNotes(null);
		}
	}

	/**
	 * if a structure has multiple constraints, put them in the natural order they occur in the text
	 * @param result
	 */
	private void phraseUpConstraints(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				String constraints = ((BiologicalEntity)element).getConstraint();
				if(constraints!=null && (constraints.contains(";")||constraints.contains(" "))){
					constraints = order(constraints, sentence, ((BiologicalEntity)element).getNameOriginal());
					((BiologicalEntity)element).setConstraint(constraints);
				}
			}
		}
	}

	/**
	 * this method removes any distributed constraints (primary basal stems and [primary basal] leaves) 
	 * it does not taken into consideration of possible punctuation marks that may separate constraint from structure name in the sentence.
	 * @param constraints
	 * @param sentence
	 * @param nameOriginal
	 * @return
	 */
	private String order(String constraints, String sentence,
			String nameOriginal) {
		ArrayList<String> sent = new ArrayList<String>(Arrays.asList(sentence.split("\\s+")));
		ArrayList<String> constr = new ArrayList<String>(Arrays.asList(constraints.split("\\s*?[; ]\\s*")));
		ArrayList<String> orderedCandidates = new ArrayList<String>();

		int i = sent.indexOf(nameOriginal); //i could be 0
		if(i<0){ //nameOrginal may be added by fixInner, for example. 
			if(sentence.matches(".*?\\b"+constraints+"\\b.*")){
				log(LogLevel.DEBUG, "BiologicalEntity constraints ["+constraints+"] normalized to itself [entity name not present in sentence]");
				return constraints;
			}
		}
		do{
			String ordered = "";
			if(i>=constr.size()){
				for(int j = i - constr.size(); j<i; j++){
					int sizeBefore = constr.size();
					constr.remove(sent.get(j));
					if(constr.size()<sizeBefore)
						ordered = ordered +" " + sent.get(j);
				}
			}
			if(constr.isEmpty()){
				if(constraints.compareTo(ordered)!=0)
					log(LogLevel.DEBUG, "BiologicalEntity constraints ["+constraints+"] perfectly normalized to "+ ordered);
				return ordered.trim();
			}else{
				orderedCandidates.add(ordered.trim());
			}
			//index of the next occurrence of the nameOrginal
			for(int j = 0; j <=i; j++){
				sent.set(j, " ");
			}

			i = sent.indexOf(nameOriginal);
		}while(i>=constr.size());

		int max = 0;
		String selected = "";
		for(String ordered: orderedCandidates){
			//return the longest
			if(max < ordered.trim().split("\\s+").length){
				max = ordered.trim().split("\\s+").length;
				selected = ordered;
			}
		}
		if(constraints.compareTo(selected)!=0 && !selected.isEmpty()){
			log(LogLevel.DEBUG, "BiologicalEntity constraints ["+constraints+"] somehow normalized to "+ selected +" for sentence ["+sentence+"]");
		}else if(constraints.compareTo(selected)!=0 && selected.isEmpty()){
			if(sentence.matches(".*?\\b"+constraints+"\\b.*")){
				log(LogLevel.DEBUG, "BiologicalEntity constraints ["+constraints+"] normalized to itself");
				return constraints;
			}
		}
		return selected.trim();
	}

	/**
	 * separate quantity from presence
	 * @param result
	 */
	private void quantityVsPresence(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				for(Character c: chars){
					//if(c.getName().compareTo("quantity")==0 && c.getValue()!=null && c.getValue().matches(".*?\\b(absent|present|0)\\b.*")){
					if(c.getName().compareTo("quantity")==0 && c.getValue()!=null && c.getValue().matches("absent|present|0")){
						c.setName("presence");
						c.setValue(c.getValue().replaceAll("0", "absent"));
					}

				}
			}
		}
	}


	private void renameCharacter(LinkedList<Element> result, String oldName,
			String newName) {
		for(Element element: result){
			if(element.isStructure()){
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				for(Character c: chars){
					if(c.getName().compareTo(oldName)==0){
						c.setName(newName);
					}
				}
			}
		}
	}

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
	 * 
	 * if character's is_modifier attribute = true and (the structure has some other is_modifier = false characters || character is an entityStructuralConstraintElement)
	 * then make the character a structure constraint
	 * @param result
	 */

	/*
	private void character2structureContraint(LinkedList<Element> result) {
		for(Element element: result){
			if(element.isStructure()){
				String oid = ((BiologicalEntity)element).getId();
				LinkedHashSet<Character> chars = ((BiologicalEntity)element).getCharacters();
				List<Character> removes = new ArrayList<Character>();
				boolean hasIsModifierCharacter = false;
				boolean hasIsNotModifierCharacter = false;
				ArrayList<Character> constraints = new ArrayList<Character> ();
				for(Character c: chars){
					if(c.getIsModifier()!=null && c.getIsModifier().compareTo("true")==0){
						hasIsModifierCharacter = true;
						constraints.add(c);
						if(c.getName()!=null && c.getName().matches(".*?(^|_or_)("+ElementRelationGroup.entityStructuralConstraintElements+")(_or_|$).*")){
							if(c.getValue()!=null){ //convert to constraint
								String newConstraint = c.getValue();
								String existingConstraint = ((BiologicalEntity) element).getConstraint();
								if(existingConstraint==null || (!existingConstraint.matches(".*?(^|; )"+newConstraint+"($|;).*"))){
									((BiologicalEntity) element).setConstraint(newConstraint+ (existingConstraint!=null? ";"+existingConstraint: ""));
								}
								removes.add(c);
							}
						}
					}else{
						hasIsNotModifierCharacter = true;
					}
				}	
				if(hasIsModifierCharacter && hasIsNotModifierCharacter){
					for(Character c: constraints){
						if(c.getValue()!=null && c.getValue().matches("[a-zA-Z]+.*")&& !c.getName().matches("count|quantity ") && c.getUnit()==null){//convert to constraint
							String newConstraint = c.getValue();
							String existingConstraint = ((BiologicalEntity) element).getConstraint();
							if(existingConstraint==null || (!existingConstraint.matches(".*?(^|; )"+newConstraint+"($|;).*"))){
								((BiologicalEntity) element).setConstraint(newConstraint+ (existingConstraint!=null? ";"+existingConstraint: ""));
							}
							removes.add(c);
						}
					}
				}else{
					//do nothing
					for(Character c: constraints){
						System.out.println("character "+c.toString()+" is not converted");
					}
				}
				chars.removeAll(removes);
			}
		}

	}*/

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
						//&& ((BiologicalEntity)element).getConstraint()!=null && ((BiologicalEntity)element).getConstraint().matches("^(no|not|never)\\b.*")){
						&& ((BiologicalEntity)element).getConstraint()!=null && ((BiologicalEntity)element).getConstraint().matches("(no|not|never)")){//constraint is a single adv
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
						//&& ((BiologicalEntity)element).getConstraint()!=null && posKnowledgeBase.isAdverb(constraint.contains(" ")? constraint.substring(0, constraint.indexOf(" ")): constraint)){
						&& ((BiologicalEntity)element).getConstraint()!=null && !constraint.contains(" ") &&!constraint.contains(";") && posKnowledgeBase.isAdverb(constraint)){ //constraint is a single adverb

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
