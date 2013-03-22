package semanticMarkup.io.input.lib.eval;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Structure {

	private String name;
	private String id;
	private String constraint;
	private ArrayList<Character> character = new ArrayList<Character>();
	
	public Structure() { } 
	
	public Structure(String name, String id, String constraint, ArrayList<Character> character) {
		super();
		this.name = name;
		this.id = id;
		this.constraint = constraint;
		this.character = character;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	@XmlElement
	public ArrayList<Character> getCharacter() {
		return character;
	}

	public void setCharacter(ArrayList<Character> character) {
		this.character = character;
	}
}
