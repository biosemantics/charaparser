package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Collapses multiple character elements of the same biological entity element that refer to the same identity into a single character element.
 * The element that appears first in the document out of the set of documents that refer to the same identity is retained.
 */
public class CollapseCharacters extends AbstractTransformer {

	private class Identity {
		
		private String charType;
		private String constraint;
		private String modifier;
		private String name;
		private String ontologyid;
		
		public Identity(Element character) {
			this.charType = character.getAttributeValue("char_type");
			this.charType = charType == null ? "" : charType;
			this.constraint = character.getAttributeValue("constraint");
			this.constraint = constraint == null ? "" : constraint;
			this.modifier = character.getAttributeValue("modifier");
			this.modifier = modifier == null ? "" : modifier;
			this.name = character.getAttributeValue("name");
			this.name = name == null ? "" : name;
			this.ontologyid = character.getAttributeValue("ontologyid");
			this.ontologyid = ontologyid == null ? "" : ontologyid;
		}

		public Identity(String charType, String constraint, String modifier, String name, String ontologyid) {
			super();
			this.charType = charType;
			this.constraint = constraint;
			this.modifier = modifier;
			this.name = name;
			this.ontologyid = ontologyid;
		}
		
	}
	
	@Override
	public void transform(Document document) {
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document))
			collapseCharacters(biologicalEntity);
	}

	private void collapseCharacters(Element biologicalEntity) {
		Map<Identity, Set<Element>> identityElementsMap = createIdentityElementsMap(biologicalEntity);
		for(Identity identity : identityElementsMap.keySet()) {
			Element representative = null;
			for(Element character : identityElementsMap.get(identity)) {
				if(representative == null)
					representative = biologicalEntity;
				else
					collapseCharacter(character, representative);
			}
		}
	}

	private void collapseCharacter(Element character, Element representative) {
		String representativeValue = representative.getAttributeValue("value");
		String value = character.getAttributeValue("value");
		if(representativeValue == null)
			representativeValue = "";
		representativeValue = representativeValue.trim();
		if(representativeValue.isEmpty())
			representativeValue = value;
		else
			representativeValue += " ; " + value;
	}

	private Map<Identity, Set<Element>> createIdentityElementsMap(Element biologicalEntity) {
		Map<Identity, Set<Element>> result = new HashMap<Identity, Set<Element>>();
		for(Element character : biologicalEntity.getChildren("character")) {
			Identity identity = new Identity(character);
			if(!result.containsKey(identity))
				result.put(identity, new LinkedHashSet<Element>());
			result.get(identity).add(character);
		}
		return result;
	}

}
