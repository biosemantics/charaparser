package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class RemoveNonSpecificBiologicalEntitiesByBackwardConnectors extends RemoveNonSpecificBiologicalEntities {

	private String connectBackwardToParent = "has|have|with|contains";
	
	public RemoveNonSpecificBiologicalEntitiesByBackwardConnectors(
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
						String parent = findParentConnectedByBackwardKeyWords(name, biologicalEntity, passedStatements, document);
						if(parent != null) {
							constraint = (constraint += " " + parent).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					}
				}
			}
		}
	}

	private String findParentConnectedByBackwardKeyWords(String name, Element biologicalEntity, List<Element> passedStatements, Document document) {
		String nameOriginal = biologicalEntity.getAttributeValue("name_original");
		
		Element statement = passedStatements.get(0);
		String sentence = statement.getChild("text").getValue();
		List<Token> terms = tokenizer.tokenize(sentence);
			
		int termPosition = terms.indexOf(new Token(nameOriginal)) - 1;
		if(termPosition != -1) {
			for(; termPosition >= 0; termPosition--) {
				String previousTerm = terms.get(termPosition).getContent();
				if(isConnectBackwardToParentKeyWord(previousTerm)) {
					return findPreceedingParentInSentence(termPosition, nameOriginal, passedStatements);
				}
			}
		}
		
		return null;
	}

	private String findPreceedingParentInSentence(int connectorPosition, String term, List<Element> passedStatements) {
		boolean firstStatement = true;
		for(Element passedStatement : passedStatements) {
			String sentence = passedStatement.getChild("text").getValue();
			List<Token> terms = tokenizer.tokenize(sentence);
			
			int termPosition = terms.size() - 1;
			if(firstStatement) {
				if(connectorPosition - 1 >= 0)
					termPosition = connectorPosition - 1;
				firstStatement = false;
			} 
			for(; termPosition >= 0; termPosition--) {
				String previousTerm = terms.get(termPosition).getContent();
				Element biologicalEntity = findBiologicalEntityByNameInStatement(previousTerm, passedStatement);
				
				if(biologicalEntity != null) {
					String nameNormalized = biologicalEntity.getAttributeValue("name");
					if(knowsPartOf.isPartOf(term, nameNormalized)) 
						return collapseBiologicalEntityToName.collapse(biologicalEntity);
				}
			}		
		}
		return null;
	}
	
	private boolean isConnectBackwardToParentKeyWord(String term) {
		return term.matches(connectBackwardToParent);
	}

}
