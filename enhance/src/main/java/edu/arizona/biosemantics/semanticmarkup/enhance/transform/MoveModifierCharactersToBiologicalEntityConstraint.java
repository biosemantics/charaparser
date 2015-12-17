package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsEntityExistence;

/**
 * Moves constraint information represented by a is_modifier character to its parent biological entity if there is an ontology match
 * 
 * sentence: long red leaves toothed
 * long is_modifier character
 * red is_modifier character
 * toothed character
 * check ontology if "long red leaves" contained, then incrementally remove and swap to find matches
 * Biggest match favored.
 * Commas break the addition of is_modifier characters as constraint
 * 
 */
public class MoveModifierCharactersToBiologicalEntityConstraint extends AbstractTransformer {

	private ITokenizer tokenizer;
	private KnowsEntityExistence knowsEntityExistence;

	public MoveModifierCharactersToBiologicalEntityConstraint(ITokenizer tokenizer, KnowsEntityExistence knowsEntityExistence) {
		this.tokenizer = tokenizer;
		this.knowsEntityExistence = knowsEntityExistence;
	}
	
	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChild("text").getValue().toLowerCase();
			List<Token> tokens = tokenizer.tokenize(sentence);
			
			for(Element biologicalEntity : statement.getChildren("biological_entity")) {
				List<Element> possibleConstraintCharacters = getPossibleConstraintCharacters(tokens, biologicalEntity);
				List<Element> applicableConstraintCharacters = getApplicableConstraints(possibleConstraintCharacters, biologicalEntity);
				
				String prependConstraint = "";
				for(Element applicableConstraintCharacter : applicableConstraintCharacters) {
					prependConstraint += " " + applicableConstraintCharacter.getAttributeValue("value");
					applicableConstraintCharacter.detach();
				}
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint;
				constraint = (prependConstraint + " " + constraint).trim();
				biologicalEntity.setAttribute("constraint", constraint);
			}
		}
	}

	private List<Element> getApplicableConstraints(List<Element> possibleConstraintCharacters, Element biologicalEntity) {
		String name = biologicalEntity.getAttributeValue("name");
		name = name == null ? "" : name;
		ICombinatoricsVector<Element> parts = Factory.createVector(possibleConstraintCharacters);
		Generator<Element> permutationGenerator = Factory.createPermutationGenerator(parts);
		
		List<Element> candiateResult = new LinkedList<Element>();
		for(ICombinatoricsVector<Element> permutation : permutationGenerator) {
			LinkedHashMap<String, List<Element>> constraintStringsMap = getConstraintStrings(permutation);
			for(String constraint : constraintStringsMap.keySet()) {
				String entityName = constraint + " " + name;
				if(knowsEntityExistence.isExistsEntity(entityName)) {
					List<Element> elementList = constraintStringsMap.get(constraint);
					if(elementList.size() > candiateResult.size())
						candiateResult = elementList;
				}
			}
		}
		return candiateResult;
	}

	private LinkedHashMap<String, List<Element>> getConstraintStrings(ICombinatoricsVector<Element> permutation) {
		LinkedHashMap<String, List<Element>> result = new LinkedHashMap<String, List<Element>>();
		List<Element> vector = permutation.getVector();
		
		for(int length = vector.size(); length >= 1; length--) {
			List<Element> constraintList = new LinkedList<Element>();
			for(int i = length; i >= 1; i--) {
				Element element = vector.get(vector.size() - i);
				constraintList.add(0, element);
			}
			result.put(getConstraint(constraintList), constraintList);
		}
		
		return result;
	}

	private String getConstraint(List<Element> constraintList) {
		String result = "";
		for(Element character : constraintList) {
			String value = character.getAttributeValue("value");
			value = value == null ? "" : value;
			result += value + " ";
		}
		return result.trim();
	}

	private List<Element> getPossibleConstraintCharacters(List<Token> tokens, Element biologicalEntity) {
		List<Element> possibleConstraintCharacters = new LinkedList<Element>();
		
		Token token = getTokenOfBiologicalEntity(tokens, biologicalEntity);
		if(token != null) {
			int tokenIndex = tokens.indexOf(token) - 1;
			for(; tokenIndex >= 0; tokenIndex--) {
				Element character = getCharacter(tokens.get(tokenIndex), biologicalEntity);
				if(character != null) {
					String isModifier = character.getAttributeValue("is_modifier");
					isModifier = isModifier == null ? "" : isModifier;
					if(isModifier.equals("true")) {
						possibleConstraintCharacters.add(character);
					} else {
						break;
					}
				} else {
					break;
				}
			}
		}
		return possibleConstraintCharacters;
	}

	private Element getCharacter(Token token, Element biologicalEntity) {
		for(Element character : biologicalEntity.getChildren("character")) {
			String value = character.getAttributeValue("value");
			value = value == null ? "" : value;
			if(value.equals(token.getContent())) {
				return character;
			}
		}
		return null;
	}

	private Token getTokenOfBiologicalEntity(List<Token> tokens, Element biologicalEntity) {
		String nameOriginal = biologicalEntity.getAttributeValue("name_original");
		nameOriginal = nameOriginal == null ? "" : nameOriginal;
		for(Token token : tokens) {
			if(nameOriginal.equals(token.getContent())) {
				return token;
			}
		}
		return null;
	}	

}
