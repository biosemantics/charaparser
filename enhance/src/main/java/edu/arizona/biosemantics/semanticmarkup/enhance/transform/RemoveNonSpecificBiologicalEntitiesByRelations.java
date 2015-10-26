package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class RemoveNonSpecificBiologicalEntitiesByRelations extends RemoveNonSpecificBiologicalEntities {

	private CollapseBiologicalEntityToName collapseBilogicalEntityToName;

	public RemoveNonSpecificBiologicalEntitiesByRelations(
			KnowsPartOf knowsPartOf, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBilogicalEntityToName) {
		super(knowsPartOf, tokenizer);
		this.collapseBilogicalEntityToName = collapseBilogicalEntityToName;
	}

	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			for(Element relation : statement.getChildren("relation")) {
				if(relation.getAttributeValue("name").equals("part_of")) {
					Element toBiologicalEntity = this.getBiologicalEntityWithId(document, relation.getAttributeValue("to"));
					Element fromBiologicalEntity = this.getBiologicalEntityWithId(document, relation.getAttributeValue("from"));
					
					String constraint = fromBiologicalEntity.getAttributeValue("constraint");
					constraint = constraint == null ? "" : constraint.trim();
					String parent = collapseBilogicalEntityToName.collapse(toBiologicalEntity);
					if(parent != null) {
						constraint = (parent + " " + constraint).trim();
						fromBiologicalEntity.setAttribute("constraint", constraint);
					}
				}
			}
		}
	}

}
