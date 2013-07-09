package semanticMarkup.model;

import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.Meta;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Statement;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.markupElement.habitat.model.Habitat;

public abstract class Element {
	
	public boolean isRelation() {
		return this instanceof Relation;
	}
	
	public boolean isCharacter() {
		return this instanceof Character;
	}
	
	public boolean isStructure() {
		return this instanceof Structure;
	}
	
	public boolean isDescription() {
		return this instanceof Description;
	}

	public boolean isStatement() {
		return this instanceof Statement;
	}
	
	public boolean isMeta() {
		return this instanceof Meta;
	}
	
	public boolean isHabitat() {
		return this instanceof Habitat;
	}
	
	public boolean isOfType(Class<? extends Element> elementType) {
		return this.getClass().equals(elementType);
	}

	public boolean isNamedElement() {
		return this instanceof NamedElement;
	}

	public abstract void removeElementRecursively(Element element);

}
