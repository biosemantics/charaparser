package semanticMarkup.markupElement.description.eval.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Structure;


@XmlRootElement(name="description")
//@XmlType(propOrder={"source", "structures", "relations"})
public class Description {
	
	private String source;

	private Map<String, Relation> idRelationMap = new HashMap<String, Relation>();
	private Map<String, Structure> idStructureMap = new HashMap<String, Structure>();
	
	@XmlPath("statement/structure")
	private List<Structure> structures = new LinkedList<Structure>();
	
	@XmlPath("statement/relation")
	private List<Relation> relations = new LinkedList<Relation>();
	
	private List<Character> characters = new LinkedList<Character>();
	
	public Description() { } 
	
	public List<Structure> getStructures() {
		initializeModel();
		return structures;
	}
	public void setStructures(List<Structure> structures) {
		initializeModel();
		this.structures = structures;
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
		for(Structure structure : structures) {
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
