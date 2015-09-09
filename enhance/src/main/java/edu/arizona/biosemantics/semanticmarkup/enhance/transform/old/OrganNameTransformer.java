package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;


public class OrganNameTransformer extends AbstractTransformer {

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

	private IInflector inflector;
	
	public OrganNameTransformer(IInflector inflector) {
		this.inflector = inflector;
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

	@Override
	public void transform(Document document) {
		enumerateCompoundOrgan(document);
		orderOrgansInDistance(document); //spiracle-epigastrium distance = epigastrium-spiracle distance
	}
	
}
