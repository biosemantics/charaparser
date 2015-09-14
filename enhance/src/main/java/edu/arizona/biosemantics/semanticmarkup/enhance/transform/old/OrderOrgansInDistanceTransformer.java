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


public class OrderOrgansInDistanceTransformer extends AbstractTransformer {
	

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
