package semanticMarkup.io.input.lib.eval;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Statement {

	private String id;
	private String text;
	private ArrayList<Structure> structure = new ArrayList<Structure>();
	private ArrayList<Relation> relation = new ArrayList<Relation>();
	
	public Statement() { }

	public Statement(String id, String text, ArrayList<Structure> structure,
			ArrayList<Relation> relation) {
		super();
		this.id = id;
		this.text = text;
		this.structure = structure;
		this.relation = relation;
	}
	
	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@XmlElement
	public ArrayList<Structure> getStructure() {
		return structure;
	}

	public void setStructure(ArrayList<Structure> structure) {
		this.structure = structure;
	}

	@XmlElement
	public ArrayList<Relation> getRelation() {
		return relation;
	}

	public void setRelation(ArrayList<Relation> relation) {
		this.relation = relation;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id " + id + "\n");
		sb.append("text" + text + "\n");
		sb.append("structures " + structure.toString() + "\n");
		sb.append("relations " + relation.toString() + "\n");
		return sb.toString();
	}
}
