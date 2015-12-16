package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * If biological entity contains a character distance = ?, and the biological entity has a name containing a hyphen:
 * Sort the parts of the name separated by the hyphen alphabetically.
 */
public class SortBiologicalEntityNameWithDistanceCharacter extends AbstractTransformer {
	

	/**
	 * spiracle-epigastrium distance = epigastrium-spiracle distance, 
	 * sort the involving organs alphabetically
	 * @param result
	 */
	@Override
	public void transform(Document document) {
		 //spiracle-epigastrium distance = epigastrium-spiracle distance
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				if(character.getAttributeValue("name").equals("distance")){
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
	
}
