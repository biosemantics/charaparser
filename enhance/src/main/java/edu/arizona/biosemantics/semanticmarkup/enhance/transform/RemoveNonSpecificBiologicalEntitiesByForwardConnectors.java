package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class RemoveNonSpecificBiologicalEntitiesByForwardConnectors extends RemoveNonSpecificBiologicalEntities {

	private String connectForwardToParent = "in|on|of";
	
	public RemoveNonSpecificBiologicalEntitiesByForwardConnectors(
			KnowsPartOf knowsPartOf, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName) {
		super(knowsPartOf, tokenizer, collapseBiologicalEntityToName);
	}
	
	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			for(Element biologicalEntity : statement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						String parent = findParentConnectedByForwardKeyWords(name, biologicalEntity, statement, document);
						if(parent != null) {
							constraint = (constraint += " " + parent).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					}
				}
			}
		}
	}
	
	private String findParentConnectedByForwardKeyWords(String name, Element biologicalEntity, Element statement, Document document) {
		String nameOriginal = biologicalEntity.getAttributeValue("name_original");
		String sentence = statement.getChild("text").getValue();
		List<Token> terms = tokenizer.tokenize(sentence);
		
		int termPosition = terms.indexOf(new Token(nameOriginal));
		if(termPosition != -1) {
			for(; termPosition < terms.size() - 1; termPosition++) {
				String nextTerm = terms.get(termPosition + 1).getContent();
				if(isConnectForwardToParentKeyWord(nextTerm)) {
					return findFollowingParentInSentence(termPosition, nameOriginal, sentence, statement);
				}
			}
		}
		return null;
	}

	private String findFollowingParentInSentence(int connectorPosition, String term, String sentence, Element statement) {
		List<Token> terms = tokenizer.tokenize(sentence);
		for(int termPosition = connectorPosition + 1; termPosition < terms.size(); termPosition ++) {
			String followingTerm = terms.get(termPosition).getContent();
			Element biologicalEntity = findBiologicalEntityByNameInStatement(followingTerm, statement);
			
			if(biologicalEntity != null) {
				String nameNormalized = biologicalEntity.getAttributeValue("name");
				if(knowsPartOf.isPartOf(term, nameNormalized)) 
					return collapseBiologicalEntityToName.collapse(biologicalEntity);
			}
		}
		return null;
	}

	private boolean isConnectForwardToParentKeyWord(String term) {
		return term.matches(connectForwardToParent);
	}

}
