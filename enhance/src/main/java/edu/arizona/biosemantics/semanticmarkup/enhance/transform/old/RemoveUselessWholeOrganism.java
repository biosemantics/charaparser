package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

public class RemoveUselessWholeOrganism extends AbstractTransformer {

		//if unknown_subject has no characters and no relations, remove them.
		@Override
		public void transform(Document document) {
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
