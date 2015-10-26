package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Collapses multiple biological entity elements that refer to the same identity into a single element.
 * The element that appears first in the document out of the set of documents that refer to the same identity is retained.
 */
public class CollapseBiologicalEntities extends AbstractTransformer {
	
	private class Identity {
		
		private String constraint;
		private String name;
		private String ontologyid;
		
		public Identity(Element biologicalEntity) {
			this.constraint = biologicalEntity.getAttributeValue("constraint");
			this.constraint = constraint == null ? "" : constraint;
			this.name = biologicalEntity.getAttributeValue("name");
			this.name = name == null ? "" : name;
			this.ontologyid = biologicalEntity.getAttributeValue("ontologyid");
			this.ontologyid = ontologyid == null ? "" : ontologyid;
		}
		
		public Identity(String constraint, String name, String ontologyid) {
			super();
			this.constraint = constraint;
			this.name = name;
			this.ontologyid = ontologyid;
		}
		
		public String getConstraint() {
			return constraint;
		}

		public String getName() {
			return name;
		}

		public String getOntologyid() {
			return ontologyid;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((constraint == null) ? 0 : constraint.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((ontologyid == null) ? 0 : ontologyid.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Identity other = (Identity) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (constraint == null) {
				if (other.constraint != null)
					return false;
			} else if (!constraint.equals(other.constraint))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (ontologyid == null) {
				if (other.ontologyid != null)
					return false;
			} else if (!ontologyid.equals(other.ontologyid))
				return false;
			return true;
		}
		private CollapseBiologicalEntities getOuterType() {
			return CollapseBiologicalEntities.this;
		}
	}
	
	@Override
	public void transform(Document document) {
		Map<Identity, Set<Element>> identityElementsMap = createIdentityElementsMap(document);
		for(Identity identity : identityElementsMap.keySet()) {
			Element representative = null;
			for(Element biologicalEntity : identityElementsMap.get(identity)) {
				if(representative == null)
					representative = biologicalEntity;
				else
					collapseBiologicalEntity(biologicalEntity, representative);
			}
		}
	}

	private void collapseBiologicalEntity(Element biologicalEntity,	Element representative) {
		for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
			character.detach();
			representative.addContent(character);
		}
		biologicalEntity.detach();
	}

	private Map<Identity, Set<Element>> createIdentityElementsMap(Document document) {
		Map<Identity, Set<Element>> result = new HashMap<Identity, Set<Element>>();
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			Identity identity = new Identity(biologicalEntity);
			if(!result.containsKey(identity))
				result.put(identity, new LinkedHashSet<Element>());
			result.get(identity).add(biologicalEntity);
		}
		return result;
	}	
	
}
