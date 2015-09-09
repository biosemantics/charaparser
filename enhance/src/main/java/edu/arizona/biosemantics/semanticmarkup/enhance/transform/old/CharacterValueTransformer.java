package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class CharacterValueTransformer extends AbstractTransformer {

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
			if (!biologicalEntity.getAttributeValue("name").contains("/")) // add all character  values also contain /
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

	private IInflector inflector;
	
	public CharacterValueTransformer(IInflector inflector) {
		this.inflector = inflector;
	}
	
	
	@Override
	public void transform(Document document) {
		enumerateCompoundStates(document);
		normalizeZeroCount(document);
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
	 * 	nomarlization count
	 *  count = "none" =>count = 0; 
	 *  count = "absent" =>count = 0;
	 *  count = "present", modifier = "no|not|never" =>count = 0;
	 *  
	 * @param xml
	 */
	private void normalizeZeroCount(Document document) {
		for(Element structure : this.biologicalEntityPath.evaluate(document)) {
			for(Element character : structure.getChildren("character")) {
				String name = character.getAttributeValue("name");
				String value = character.getAttributeValue("value");
				String modifier = character.getAttributeValue("modifier");
				if(name != null && name.compareTo("count")==0) {
					if(value != null){
						if(value.compareTo("none") == 0) 
							character.setAttribute("value", "0"); 
						if(value.compareTo("absent") == 0 && (modifier == null || !modifier.matches("no|not|never"))) 
							character.setAttribute("value", "0"); 
						if(value.compareTo("present") == 0 && modifier !=null && modifier.matches("no|not|never")) { 
							character.setAttribute("value", "0");
							character.setAttribute("modifier", "");
						}
					}
				}	
			}
		}
	}
	
}
