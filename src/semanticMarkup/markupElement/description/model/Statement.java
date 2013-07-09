package semanticMarkup.markupElement.description.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import semanticMarkup.model.Element;
import semanticMarkup.model.description.attributes.StatementAttribute;

public class Statement extends Element {
	
	@XmlPath("@" + StatementAttribute.text)
	private String text;
	
	@XmlPath("@" + StatementAttribute.id)
	private String id;
	
	@XmlPath("@" + StatementAttribute.provenance)
	private String provenance;
	
	@XmlPath("@" + StatementAttribute.source)
	private String source;
	
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
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
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
		for(Structure structure : this.structures)
			if(structure.equals(element))
				this.structures.remove(element);
			else
				structure.removeElementRecursively(element);
		for(Relation relation : this.relations)
			if(relation.equals(element))
				this.relations.remove(element);
			else
				relation.removeElementRecursively(element);
	}
}
