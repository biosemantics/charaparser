package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;



@XmlRootElement(name="description")
//@XmlType(propOrder={"source", "biologicalEntities", "relations"})
public class Description {
	
	private String source;

	private Map<String, Relation> idRelationMap = new HashMap<String, Relation>();
	private Map<String, BiologicalEntity> idStructureMap = new HashMap<String, BiologicalEntity>();
	
	@XmlPath("statement/biological_entity")
	private List<BiologicalEntity> biologicalEntities = new LinkedList<BiologicalEntity>();
	
	@XmlPath("statement/relation")
	private List<Relation> relations = new LinkedList<Relation>();
	
	private List<Character> characters = new LinkedList<Character>();
	
	public Description() { } 
	
	public List<BiologicalEntity> getStructures() {
		initializeModel();
		return biologicalEntities;
	}
	public void setStructures(List<BiologicalEntity> structures) {
		initializeModel();
		this.biologicalEntities = structures;
	}

	public List<Relation> getRelations() {
		initializeModel();
		return relations;
	}
	
	public void setRelations(List<Relation> relations) {
		initializeModel();
		this.relations = relations;
	}

	public List<Character> getCharacters() {
		initializeModel();
		return this.characters;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	private void initializeModel() {
		this.characters = new LinkedList<Character>();
		for(BiologicalEntity structure : biologicalEntities) {
			this.idStructureMap.put(structure.getId(), structure);
			for(Character character : structure.getCharacters()) 
				character.setStructure(structure);
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
	}

}
