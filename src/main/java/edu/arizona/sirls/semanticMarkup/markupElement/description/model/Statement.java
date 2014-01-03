package edu.arizona.sirls.semanticMarkup.markupElement.description.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.sirls.semanticMarkup.markupElement.description.model.attributes.StatementAttribute;
import edu.arizona.sirls.semanticMarkup.model.Element;


public class Statement extends Element {
	
	@XmlPath("@" + StatementAttribute.text)
	private String text;
	
	@XmlPath("@" + StatementAttribute.id)
	private String id;
	
	@XmlPath("@" + StatementAttribute.provenance)
	private String provenance;
	
	@XmlPath("@" + StatementAttribute.notes)
	private String notes;

	
	private List<Structure> structures = new LinkedList<Structure>();
	private List<Relation> relations = new LinkedList<Relation>();
	
	public List<Structure> getStructures() {
		return structures;
	}
	public void setStructures(List<Structure> structures) {
		this.structures = structures;
	}

	public List<Relation> getRelations() {
		return relations;
	}
	
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProvenance() {
		return provenance;
	}
	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void addRelation(Relation relation) {
		this.relations.add(relation);
	}
	public void addStructure(Structure structure) {
		this.structures.add(structure);
	}
	@Override
	public void removeElementRecursively(Element element) {
		Iterator<Structure> structuresIterator = structures.iterator();
		while(structuresIterator.hasNext()) {
			Structure structure = structuresIterator.next();
			if(structure.equals(element))
				structuresIterator.remove();
			else
				structure.removeElementRecursively(element);
		}
		
		Iterator<Relation> relationsIterator = relations.iterator();
		while(relationsIterator.hasNext()) {
			Relation relation = relationsIterator.next();
			if(relation.equals(element))
				relationsIterator.remove();
			else
				relation.removeElementRecursively(element);
		}
	}
}
