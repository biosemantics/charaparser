package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class RemoveNonSpecificBiologicalEntitiesByPassedParents extends RemoveNonSpecificBiologicalEntities {
	
	public RemoveNonSpecificBiologicalEntitiesByPassedParents(
			KnowsPartOf knowsPartOf, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName) {
		super(knowsPartOf, tokenizer, collapseBiologicalEntityToName);
	}

	@Override
	public void transform(Document document) {
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			passedStatements.add(0, statement);
			for(Element biologicalEntity : statement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						String parent = findParentInPassedStatements(name, document, passedStatements);
						if(parent != null) {
							constraint = (constraint += " " + parent).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					} 
				}
			}
		}
	}

	private String findParentInPassedStatements(String name, Document document, List<Element> passedStatements) {
		for(Element passedStatement : passedStatements) {
			String sentence = passedStatement.getChild("text").getValue().toLowerCase();
			for(Token term : tokenizer.tokenize(sentence)) {
				Element termsBiologicalEntity = getBiologicalEntity(term.getContent(), document);
				
				if(termsBiologicalEntity != null) {
					String termNormalized = termsBiologicalEntity.getAttributeValue("name");
					if(knowsPartOf.isPartOf(name, termNormalized)) {
						return collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
					}
				}
			}
		}
		return null;
	}

	private Element getBiologicalEntity(String term, Document document) {
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String name = biologicalEntity.getAttributeValue("name_original");
			name = name == null ? "" : name.trim();
			if(name.equals(term)) {
				return biologicalEntity;
			}
		}
		return null;
	}
}
