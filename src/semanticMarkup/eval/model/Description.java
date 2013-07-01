package semanticMarkup.eval.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlRootElement(name="description")
//@XmlType(propOrder={"source", "structures", "relations"})
public class Description {

	private Map<String, Relation> idRelationMap = new HashMap<String, Relation>();
	private Map<String, Structure> idStructureMap = new HashMap<String, Structure>();
	
	@XmlPath("statement/structure")
	private List<Structure> structures = new LinkedList<Structure>();
	
	@XmlPath("statement/relation")
	private List<Relation> relations = new LinkedList<Relation>();
	
	private List<Character> characters = new LinkedList<Character>();
	
	public Description() { } 
	
	public List<Structure> getStructures() {
		return structures;
	}
	public void setStructures(List<Structure> structures) {
		for(Structure structure : structures) {
			this.idStructureMap.put(structure.getId(), structure);
			this.characters.addAll(structure.getCharacters());
		}
		for(Relation relation : this.relations) {
			if(idStructureMap.containsKey(relation.getFrom()))
				idStructureMap.get(relation.getFrom()).addFromRelation(relation);
				relation.setFromStructure(idStructureMap.get(relation.getFrom()));
			if(idStructureMap.containsKey(relation.getTo())) {
				idStructureMap.get(relation.getFrom()).addToRelation(relation);
				relation.setToStructure(idStructureMap.get(relation.getFrom()));
			}
		}
		this.structures = structures;
	}
	public List<Relation> getRelations() {
		return relations;
	}
	public void setRelations(List<Relation> relations) {
		for(Relation relation : relations)
			this.idRelationMap.put(relation.getId(), relation);
		for(Relation relation : this.relations) {
			if(idStructureMap.containsKey(relation.getFrom()))
				idStructureMap.get(relation.getFrom()).addFromRelation(relation);
				relation.setFromStructure(idStructureMap.get(relation.getFrom()));
			if(idStructureMap.containsKey(relation.getTo())) {
				idStructureMap.get(relation.getFrom()).addToRelation(relation);
				relation.setToStructure(idStructureMap.get(relation.getFrom()));
			}
		}
		this.relations = relations;
	}

	public List<Character> getCharacters() {
		return this.characters;
	}

}
