package semanticMarkup.io.output.lib.xml2;

import javax.xml.bind.annotation.XmlAttribute;

public class Relation {

	private String name;
	private String id;
	private String from;
	private String to;
	private String negation;
	private String modifier;
	
	public Relation() { }
	
	public Relation(String name, String id, String from, String to,
			String negation, String modifier) {
		super();
		this.name = name;
		this.id = id;
		this.from = from;
		this.to = to;
		this.negation = negation;
		this.modifier = modifier;
	}
	
	@XmlAttribute
	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
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
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@XmlAttribute
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@XmlAttribute
	public String getNegation() {
		return negation;
	}

	public void setNegation(String negation) {
		this.negation = negation;
	} 
}


