package edu.arizona.biosemantics.semanticmarkup.model;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.log.Logger;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Meta;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.Habitat;


public abstract class Element {
	
	private static Marshaller marshaller = null; 
	
	static {
		try {
			JAXBContext jaxbContext = JAXBContextFactory.createContext(
					new Class[] {Element.class, Statement.class, BiologicalEntity.class, Character.class, Relation.class }, 
					null);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		} catch(Exception e) {
			Logger logger = Logger.getLogger(NamedElement.class);
			logger.error("Problem creating JAXB marshaller for NamedElement", e);
		}
	}
	
	public boolean isRelation() {
		return this instanceof Relation;
	}
	
	public boolean isCharacter() {
		return this instanceof Character;
	}
	
	public boolean isStructure() {
		return this instanceof BiologicalEntity;
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
	
	@Override
	public String toString() {
		try {
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(this, stringWriter);
			return stringWriter.toString();
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem converting NamedElement to String", e);
		}
		return super.toString();
	}

}
