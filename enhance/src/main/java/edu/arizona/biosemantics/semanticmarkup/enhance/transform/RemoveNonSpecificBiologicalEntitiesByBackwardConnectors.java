package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class RemoveNonSpecificBiologicalEntitiesByBackwardConnectors extends RemoveNonSpecificBiologicalEntities {

	private String connectBackwardToParent = "has|have|with|contains";
	private CollapseBiologicalEntityToName collapseBiologicalEntityToName;
	
	public RemoveNonSpecificBiologicalEntitiesByBackwardConnectors(
			KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName) {
		super(knowsPartOf, knowsSynonyms, tokenizer);
		this.collapseBiologicalEntityToName = collapseBiologicalEntityToName;
	}
	
	@Override
	public void transform(Document document) {
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			passedStatements.add(0, statement);
			Map<String, Integer> nameOccurences = new HashMap<String, Integer>();
			for(Element biologicalEntity : statement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						if(!nameOccurences.containsKey(name))
							nameOccurences.put(name, 0);
						nameOccurences.put(name, nameOccurences.get(name) + 1);
						int appearance = nameOccurences.get(name);
						
						String parent = findParentConnectedByBackwardKeyWords(name, appearance, biologicalEntity, passedStatements, document);
						if(parent != null) {
							constraint = (parent + " " + constraint).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					}
				}
			}
		}
	}

	private String findParentConnectedByBackwardKeyWords(String name, int appearance, Element biologicalEntity, List<Element> passedStatements, Document document) {
		String nameOriginal = biologicalEntity.getAttributeValue("name_original");
		
		Element statement = passedStatements.get(0);
		String sentence = statement.getChild("text").getValue().toLowerCase();
		
		/*if(name.equals("margin") && sentence.startsWith(("annulus superous, thin, membranous, simple, up to 1.8 cm broad, supramedian").toLowerCase())) {
			System.out.println();
		}*/
		
		sentence = parenthesisRemover.remove(sentence, '(', ')');
		List<Token> terms = tokenizer.tokenize(sentence);
			
		int termPosition = indexOf(terms, new Token(nameOriginal), appearance) - 1;
		if(termPosition != -1) {
			for(; termPosition >= 0; termPosition--) {
				String previousTerm = terms.get(termPosition).getContent();
				if(previousTerm.matches("\\p{Punct}")) {
					break;
				}
				if(isConnectBackwardToParentKeyWord(previousTerm)) {
					return findPreceedingParentInSentence(termPosition, nameOriginal, passedStatements);
				}
			}
		}
		
		return null;
	}
	
	private int indexOf(List<Token> tokens, Token token, int appearance) {
		int found = 0;
		for(int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			if(t.equals(token))
				found++;
			if(found == appearance)
				return i;
		}
		return -1;
	}

	private String findPreceedingParentInSentence(int connectorPosition, String term, List<Element> passedStatements) {
		boolean firstStatement = true;
		for(Element passedStatement : passedStatements) {
			String sentence = passedStatement.getChild("text").getValue().toLowerCase();
			List<Token> terms = tokenizer.tokenize(sentence);
			
			int termPosition = terms.size() - 1;
			if(firstStatement) {
				if(connectorPosition - 1 >= 0)
					termPosition = connectorPosition - 1;
				firstStatement = false;
			} 
			for(; termPosition >= 0; termPosition--) {
				String previousTerm = terms.get(termPosition).getContent();
				if(previousTerm.matches("\\p{Punct}")) {
					break;
				}
				Element biologicalEntity = findBiologicalEntityByNameInStatement(previousTerm, passedStatement);
				
				if(biologicalEntity != null) {
					String nameNormalized = collapseBiologicalEntityToName.collapse(biologicalEntity);
					if(knowsPartOf.isPartOf(term, nameNormalized)) 
						return collapseBiologicalEntityToName.collapse(biologicalEntity);
					nameNormalized = biologicalEntity.getAttributeValue("name");
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
