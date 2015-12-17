package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * Splits biological entity's who's name contains multiple ones indicated by a range of romand numbers, e.g. i-iii.
 * Characters are cloned for all newly created biological entities.
 */
public class SplitCompoundBiologicalEntity extends AbstractTransformer {
	
	private class CompoundedEntity {

		private Element biologicalEntity;

		public CompoundedEntity(Element biologicalEntity) {
			this.biologicalEntity = biologicalEntity;
		}

		public boolean isTarget() {
			return biologicalEntity.getAttributeValue("name_original") != null && 
					biologicalEntity.getAttributeValue("name_original").matches("(\\d+|[ivx]+)-(\\d+|[ivx]+)");
		}

		public List<String> getEntityParts() {
			if(biologicalEntity.getAttributeValue("name_original")==null)
				return new LinkedList<String>();
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
	
	public SplitCompoundBiologicalEntity(IInflector inflector) {
		this.inflector = inflector;
	}
	
	/**
	 * <biological_entity constraint="legs" id="o0" name="i-iii" name_original="i-iii" type="structure">
		<character constraint="than leg-iv" constraintid="o1" is_modifier="false" name="fragility" value="stronger" />
	   </biological_entity>
	 * @param result
	 */
	@Override
	public void transform(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			CompoundedEntity compoundedEntity = new CompoundedEntity(biologicalEntity);
			if(compoundedEntity.isTarget()) {
				Parent statement = biologicalEntity.getParent();
				int resultPosition = biologicalEntity.getParent().indexOf(biologicalEntity);
				biologicalEntity.detach();
				Map<String, Element> newBiologicalEntities = new LinkedHashMap<String, Element>();
				List<String> entityParts = compoundedEntity.getEntityParts();
				boolean added = false;
				
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
	

}
