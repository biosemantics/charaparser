package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class RemoveNonSpecificBiologicalEntitiesByForwardConnectors extends RemoveNonSpecificBiologicalEntities {

	private String connectForwardToParent = "in|on|of";
	private CollapseBiologicalEntityToName collapseBiologicalEntityToName;
	
	public RemoveNonSpecificBiologicalEntitiesByForwardConnectors(
			KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName) {
		super(knowsPartOf, knowsSynonyms, tokenizer);
		this.collapseBiologicalEntityToName = collapseBiologicalEntityToName;
	}
	
	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
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
						
						String parent = findParentConnectedByForwardKeyWords(name, appearance, biologicalEntity, statement, document);
						if(parent != null) {
							constraint = (parent + " " + constraint).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					}
				}
			}
		}
	}
	
	private String findParentConnectedByForwardKeyWords(String name, int appearance, Element biologicalEntity, Element statement, Document document) {
		String nameOriginal = biologicalEntity.getAttributeValue("name_original");
		String sentence = statement.getChild("text").getValue().toLowerCase();
		/*if(name.equals("side") && sentence.startsWith("breast, sides, and flanks pinkish brown contrasting with white belly and sides of rump;")) {
			System.out.println();
		}*/
		sentence = parenthesisRemover.remove(sentence, '(', ')');
		List<Token> terms = tokenizer.tokenize(sentence);
		
		int termPosition = indexOf(terms, new Token(nameOriginal), appearance);
		if(termPosition != -1) {
			for(; termPosition < terms.size() - 1; termPosition++) {
				String nextTerm = terms.get(termPosition + 1).getContent();
				if(nextTerm.matches("\\p{Punct}")) {
					break;
				}
				if(isConnectForwardToParentKeyWord(nextTerm)) {
					return findFollowingParentInSentence(termPosition, nameOriginal, sentence, statement);
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

	private String findFollowingParentInSentence(int connectorPosition, String term, String sentence, Element statement) {
		List<Token> terms = tokenizer.tokenize(sentence);
		for(int termPosition = connectorPosition + 1; termPosition < terms.size(); termPosition ++) {
			String followingTerm = terms.get(termPosition).getContent();
			if(followingTerm.matches("\\p{Punct}")) {
				break;
			}
			Element biologicalEntity = findBiologicalEntityByNameInStatement(followingTerm, statement);
			
			if(biologicalEntity != null) {
				String nameNormalized = collapseBiologicalEntityToName.collapse(biologicalEntity);
				if(knowsPartOf.isPartOf(term, nameNormalized)) 
					return collapseBiologicalEntityToName.collapse(biologicalEntity);
				nameNormalized = biologicalEntity.getAttributeValue("name");
				if(knowsPartOf.isPartOf(term, nameNormalized)) 
					return collapseBiologicalEntityToName.collapse(biologicalEntity);
				break;
			}
		}
		return null;
	}

	private boolean isConnectForwardToParentKeyWord(String term) {
		return term.matches(connectForwardToParent);
	}

}
