package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class CharacterLocationTransformer extends AbstractTransformer {
	
	@Override
	public void transform(Document document) {
		checkAlternativeIDs(document);
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
	
	private void removeAlterIDsNotes(Element charWAI) {
		String notes = charWAI.getAttributeValue("notes");
		if(notes != null && notes.contains("alterIDs:")){
			charWAI.setAttribute("notes", notes.replaceFirst("\\balterIDs:.*?(;|\\.|$)", "")); //remove alterIDs note
			if(notes != null && notes.isEmpty()) charWAI.removeAttribute("notes");
		}
	}
	

	private void moveCharacterToStructures(Element charWAI, List<String> ids, Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			if(ids.contains(biologicalEntity.getAttributeValue("id"))) {
				biologicalEntity.addContent(charWAI);
			}
		}
	}
	
}
