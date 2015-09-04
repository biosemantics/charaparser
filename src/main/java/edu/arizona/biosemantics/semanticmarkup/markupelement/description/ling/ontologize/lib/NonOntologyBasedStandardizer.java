/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;

/**
 * @author Hong Cui
 * Perform standarizations of a description paragraph without using an ontology
 * such as presence = absent => count = 0
 *        
 */
public class NonOntologyBasedStandardizer {
	
	protected XPathFactory xpathFactory = XPathFactory.instance();
	protected XPathExpression<Element> sourceXpath = 
			xpathFactory.compile("/bio:treatment/meta/source", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> taxonIdentificationXpath = 
			xpathFactory.compile("/bio:treatment/taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element(), null,
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> statementXpath = 
			xpathFactory.compile("//description[@type='morphology']/statement", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> biologicalEntityPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/biological_entity", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> relationPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/relation", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> characterPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/biological_entity/character", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	
	private Set<String> lifeStyles;
	private Set<String> durations;
	private IPOSKnowledgeBase posKnowledgeBase;
	private IInflector inflector;

	public NonOntologyBasedStandardizer(IGlossary glossary, IInflector inflector, @Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase) {
		lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		durations = glossary.getWordsInCategory("duration");
		this.posKnowledgeBase = posKnowledgeBase;
		this.inflector = inflector;
	}

	public void standardize(String directory){
		SAXBuilder saxBuilder = new SAXBuilder();
		File dir = new File(directory);
		for(File file : dir.listFiles()) {
			if(file.isFile()) {
				try {
					Document document = null;
					try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF8")) {
						document = saxBuilder.build(inputStreamReader);
					}
					
					if(document != null) 
						standardize(document);
					
					File outputFile = new File(directory, file.getName());
					try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF8")) {
						XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
						xmlOutput.output(document, outputStreamWriter);
					}
				} catch (JDOMException | IOException e) {
					log(LogLevel.ERROR, "Can't read xml from file " + file.getAbsolutePath(), e);
				}
			}
		}

	}
	
	private void standardize(Document document) {
		enumerateCompoundOrgan(document); //legs i-iii
		enumerateCompoundStates(document); // tibia/metatarsus:  1.43/1.27 mm
		orderOrgansInDistance(document); //spiracle-epigastrium distance = epigastrium-spiracle distance
		checkAlternativeIDs(document); //before count
		taxonName2WholeOrganism(document);
		createWholeOrganismDescription(document, lifeStyles, "growth_form");
		createWholeOrganismDescription(document, durations, "duration");
		//createMayBeSameRelations(result, processingContext);  //not sure we need this relation.
		removeOrphenedUnknownElements(document);
		noOrgan2AdvConstraintedOrgan(document);
		normalizeZeroCount(document);
		removeCircularCharacterConstraint(document);
		character2structureContraint(document);//is_modifier => constraint
		renameCharacter(document, "count", "quantity");
		renameCharacter(document, "atypical_count", "atypical_quantity");
		renameCharacter(document, "color", "coloration");
		quantityVsPresence(document); //after count => quantity
		phraseUpConstraints(document); //put constraints in the order as appeared in the original text, should be among the last normalization steps
		normalizeAdvConstraintedOrgan(document);	//after phraseUpConstraints
	}

	/**
	 * spiracle-epigastrium distance = epigastrium-spiracle distance, 
	 * sort the involving organs alphabetically
	 * @param result
	 */
	private void orderOrgansInDistance(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				if(character.getAttribute("name").equals("distance")){
					String biologicalEntityName = biologicalEntity.getAttributeValue("name");
					if(biologicalEntityName.contains("-")){
						String[] names = biologicalEntityName.split("\\s*-\\s*");
						Arrays.sort(names);
						biologicalEntityName = "";
						for(int n = 0; n < names.length; n++){
							biologicalEntityName += names[n]+"-";
						}
						biologicalEntity.setAttribute("name", biologicalEntityName.replaceFirst("-$", ""));
						break;
					}
				}
			}
		}
	}

	private class CompoundedEntityStates {
		private Element biologicalEntity;

		public CompoundedEntityStates(Element biologicalEntity) {
			this.biologicalEntity = biologicalEntity;
		}

		public String[] getEntityParts() {
			return biologicalEntity.getAttributeValue("name_original").split("\\s*/\\s*");
		}

		public List<String[]> getCharacterParts() {
			List<String[]> characterParts = new LinkedList<String[]>();
			for (Element character : biologicalEntity.getChildren("character"))
				if (character.getAttributeValue("value").contains("/"))
					characterParts.add(character.getAttributeValue("value").split("\\s*/\\s*"));
			return characterParts;
		}

		public boolean isTarget() {
			if (!biologicalEntity.getName().contains("/")) // add all character  values also contain /
				return false;
			String[] entityNames = this.getEntityParts();
			if (biologicalEntity.getChildren("character").isEmpty())
				return false; // distal 1/2
			List<String[]> characterParts = getCharacterParts();
			int i=0;
			for (Element character : biologicalEntity.getChildren("character")) {
				if (!character.getAttributeValue("value").contains("/"))
					return false;
				if (characterParts.get(i).length != entityNames.length)
					return false;
				i++;
			}

			return true;
		}
	}
			
	private class CompoundedEntity {

		private Element biologicalEntity;

		public CompoundedEntity(Element biologicalEntity) {
			this.biologicalEntity = biologicalEntity;
		}

		public boolean isTarget() {
			return biologicalEntity.getAttributeValue("name_original").matches(
					"(\\d+|[ivx]+)-(\\d+|[ivx]+)");
		}

		public List<String> getEntityParts() {
			String[] ends = biologicalEntity.getAttributeValue("name_original").split("-");
			ArrayList<String> individuals = new ArrayList<String>();
			String current = ends[0];
			String last = ends[ends.length - 1];
			while (current.compareTo(last) != 0) {
				individuals.add(current);
				current = nextRoman(current);
			}
			individuals.add(last);
			return individuals;
		}

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

	private void enumerateCompoundStates(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			CompoundedEntityStates compoundedEntityStates = new CompoundedEntityStates(biologicalEntity);
			if(compoundedEntityStates.isTarget()){//enumerate
				Parent parent = biologicalEntity.getParent();
				int resultPosition = parent.indexOf(biologicalEntity);
				biologicalEntity.detach();
				Map<String, Element> newBiologicalEntities = new HashMap<String, Element>();

				int id = 0;
				for (String entityName : compoundedEntityStates.getEntityParts()) {
					Element clone = biologicalEntity.clone();
					String newId = biologicalEntity.getAttributeValue("id") + "_" + id;
					clone.setAttribute("id", newId);
					clone.setAttribute("name", inflector.getSingular(entityName));
					newBiologicalEntities.put(newId, clone);

					int j=0;
					for (Element character : clone.getChildren("character")) {
						//try {
							String[] parts = compoundedEntityStates.getCharacterParts().get(j);
							//System.out.println(parts);
							character.setAttribute("value", parts[id]);
						/*} catch(Exception e) {
							System.out.println("exception");
							compoundedEntityStates.getCharacterParts();
						}*/

						j++;
					}

					parent.addContent(resultPosition + id, clone);
					id++;
				}
				updateRelations(document, biologicalEntity, newBiologicalEntities);
			}
		}
	}

	/**
	 * <biological_entity constraint="legs" id="o0" name="i-iii" name_original="i-iii" type="structure">
		<character constraint="than leg-iv" constraintid="o1" is_modifier="false" name="fragility" value="stronger" />
	   </biological_entity>
	 * @param result
	 */

	private void enumerateCompoundOrgan(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			CompoundedEntity compoundedEntity = new CompoundedEntity(biologicalEntity);
			if(compoundedEntity.isTarget()) {
				Parent statement = biologicalEntity.getParent();
				int resultPosition = biologicalEntity.getParent().indexOf(biologicalEntity);
				biologicalEntity.detach();
				Map<String, Element> newBiologicalEntities = new HashMap<String, Element>();
				List<String> entityParts = compoundedEntity.getEntityParts();
				boolean added =false;
				
				int id = 0;
				for (String individual : entityParts) {
					Element clone = biologicalEntity.clone();
					String newId = clone.getAttributeValue("id") + "_" + id;
					clone.setAttribute("name", inflector.getSingular(individual));
					clone.setAttribute("id", newId);
					newBiologicalEntities.put(newId, clone);
					
					statement.addContent(resultPosition + id, clone);
					id++;
					added = true;
				}
				if(added){					
					updateRelations(document, biologicalEntity, newBiologicalEntities);
				}
			}	
		}
	}

	private void updateRelations(Document document, Element biologicalEntity, Map<String, Element> newBiologicalEntities) {
	    //if the original element involved in any relations, individualize the relations
		//to
		String[] attributes = { "to", "from" };
		List[] relations = { this.getToRelations(biologicalEntity, document), this.getFromRelations(biologicalEntity, document) };
		
		for(int i=0; i<attributes.length; i++) {
			String attribute = attributes[i];
			List<Element> relationsList = relations[i];
			for(Element relation : relationsList) {
				Parent parent = relation.getParent();
				int relationPosition = parent.indexOf(relation);
				relation.detach();
				int rid = 0;
				for(String newId : newBiologicalEntities.keySet()){
					Element clone = relation.clone();
					clone.setAttribute("id", clone.getAttributeValue("id") + "_" + (rid));
					clone.setAttribute(attribute, newId);
					parent.addContent(relationPosition + rid, clone);
					rid++;
				}
			}
		}
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

	private void checkAlternativeIDs(Document document) {
		for (Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			ArrayList<Element> withAlterIDs = new ArrayList<Element> ();
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String notes = character.getAttributeValue("notes");
				if(notes != null && notes.contains("alterIDs:")) {
					withAlterIDs.add(character);
				}
			}

			boolean move = false;
			for(Element charWAI: withAlterIDs){
				String notes = charWAI.getAttributeValue("notes");
				removeAlterIDsNotes(charWAI);
				String note = notes.substring(notes.indexOf("alterIDs:"));
				List<String> ids = Arrays.asList(note.replaceFirst("[;\\.].*", "").replaceFirst("alterIDs:", "").split("\\s+"));
				String charName = charWAI.getAttributeValue("name"); //width, atypical_width
				if(move){
					charWAI.detach();
					moveCharacterToStructures(charWAI, ids, document);
				} else {
					for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
						if(!character.equals(charWAI) && charName.equals(character.getAttributeValue("name")) && 
								charName.matches(".*?_?(count|length|width|size|height)")){
							//move charWAI to entities with the ids
							charWAI.detach();
							moveCharacterToStructures(charWAI, ids, document); //
							move = true;
							break;
						}
					}
				}
			}
		}
	}


	private void moveCharacterToStructures(Element charWAI, List<String> ids, Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			if(ids.contains(biologicalEntity.getAttributeValue("id"))) {
				biologicalEntity.addContent(charWAI);
			}
		}
	}

	private void removeAlterIDsNotes(Element charWAI) {
		String notes = charWAI.getAttributeValue("notes");
		if(notes != null && notes.contains("alterIDs:")){
			charWAI.setAttribute("notes", notes.replaceFirst("\\balterIDs:.*?(;|\\.|$)", "")); //remove alterIDs note
			if(notes != null && notes.isEmpty()) charWAI.removeAttribute("notes");
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

	/**
	 * this method removes any distributed constraints (primary basal stems and [primary basal] leaves) 
	 * it does not taken into consideration of possible punctuation marks that may separate constraint from structure name in the sentence.
	 * @param constraints
	 * @param sentence
	 * @param nameOriginal
	 * @return
	 */
	private String order(String constraints, String sentence, String nameOriginal) {
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
			if(i >= constr.size()){
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
		} while (i >= constr.size());

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
	private void quantityVsPresence(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			//if(c.getName().compareTo("quantity")==0 && c.getValue()!=null && c.getValue().matches(".*?\\b(absent|present|0)\\b.*")){
			String name = character.getAttributeValue("name");
			String value = character.getAttributeValue("value");
			if(name.equals("quantity") && value != null && value.matches("absent|present|0")){
				character.setAttribute("name", "presence");
				character.setAttribute("value", value.replaceAll("0", "absent"));
			}
		}
	}


	private void renameCharacter(Document document, String oldName, String newName) {
		for (Element character : this.characterPath.evaluate(document)) {
			if(character.getAttributeValue("name").equals(oldName)) {
				character.setAttribute("name", newName);
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
	/**
	 * if a character constraint refers to the same structure the character belongs to, remove the constraint
	 * @param result
	 */
	private void removeCircularCharacterConstraint(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			String oid = biologicalEntity.getAttributeValue("id");
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String constraintId = character.getAttributeValue("constraintid");
				if(constraintId != null && constraintId.matches(".*?\\b" + oid + "\\b.*")) {
					character.removeAttribute("constraint");
					character.removeAttribute("constraintid");
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

	private List<Element> getFromRelations(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		for(Element relation : new ArrayList<Element>(this.relationPath.evaluate(document))) {
			if(relation.getAttributeValue("from").equals(biologicalEntity.getAttributeValue("id")))
				result.add(relation);
		}
		return result;
	}
	
	private List<Element> getToRelations(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		for(Element relation : new ArrayList<Element>(this.relationPath.evaluate(document))) {
			if(relation.getAttributeValue("to").equals(biologicalEntity.getAttributeValue("id")))
				result.add(relation);
		}
		return result;
	}
	
	private List<Element> getRelationsInvolve(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		result.addAll(this.getFromRelations(biologicalEntity, document));
		result.addAll(this.getToRelations(biologicalEntity, document));
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
					&& !constraint.contains(" ") && !constraint.contains(";") && posKnowledgeBase.isAdverb(constraint)){ //constraint is a single adverb

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


	/**
	 * 	nomarlization count
	 *  count = "none" =>count = 0; 
	 *  count = "absent" =>count = 0;
	 *  count = "present", modifier = "no|not|never" =>count = 0;
	 *  
	 * @param xml
	 */

	private void normalizeZeroCount(Document document) {
		for(Element biologicalEntity : new ArrayList<Element>(this.biologicalEntityPath.evaluate(document))) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				String name = character.getAttributeValue("name");
				String value = character.getAttributeValue("value");
				String modifier = character.getAttributeValue("modifier");
				if(name != null && name.equals("count")) {
					if(value != null){
						if(value.equals("none")) character.setAttribute("value", "0"); 
						if(value.equals("absent") && (modifier == null || !modifier.matches("no|not|never"))) 
							character.setAttribute("value", "0"); 
						if(value.equals("present") && modifier != null && modifier.matches("no|not|never")) { 
							character.setAttribute("value", "0");
							character.setAttribute("modifier", "");
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

}