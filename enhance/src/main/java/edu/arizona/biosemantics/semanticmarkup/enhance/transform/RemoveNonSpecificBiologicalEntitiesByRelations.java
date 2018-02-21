package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class RemoveNonSpecificBiologicalEntitiesByRelations extends RemoveNonSpecificBiologicalEntities {

	private CollapseBiologicalEntityToName collapseBilogicalEntityToName;

	public RemoveNonSpecificBiologicalEntitiesByRelations(
			KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBilogicalEntityToName) {
		super(knowsPartOf, knowsSynonyms, tokenizer);
		this.collapseBilogicalEntityToName = collapseBilogicalEntityToName;
	}

	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChildText("text");
			
			for(Element relation : statement.getChildren("relation")) {
				if(relation.getAttributeValue("name").equals("part_of")) {
					Element toBiologicalEntity = this.getBiologicalEntityWithId(document, relation.getAttributeValue("to"));
					Element fromBiologicalEntity = this.getBiologicalEntityWithId(document, relation.getAttributeValue("from"));					
					
					String name = fromBiologicalEntity.getAttributeValue("name");
					name = name == null ? "" : name.trim();
					String constraint = fromBiologicalEntity.getAttributeValue("constraint");
					constraint = constraint == null ? "" : constraint.trim();
					if(isNonSpecificPart(name)) {
						if(!isPartOfAConstraint(name, constraint)) {		
							String parent = null;
							String termNormalized = collapseBilogicalEntityToName.collapse(toBiologicalEntity);
							if(knowsPartOf.isPartOf(name, termNormalized)) {
								parent = collapseBilogicalEntityToName.collapse(toBiologicalEntity);
							} else {
								if(knowsPartOf.isPartOf(name, toBiologicalEntity.getAttributeValue("name"))) 
									parent = collapseBilogicalEntityToName.collapse(toBiologicalEntity);
							}
							if(parent != null) {
								constraint = (parent + " " + constraint).trim();								
								this.appendInferredConstraint(fromBiologicalEntity, parent);
								fromBiologicalEntity.setAttribute("constraint", constraint);
								fromBiologicalEntity = mergeSrc(toBiologicalEntity.getAttribute("src")==null? null:toBiologicalEntity.getAttributeValue("src"), fromBiologicalEntity);
							}
						}
					}
				}
			}
		}
	}

}
