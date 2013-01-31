package semanticMarkup.io.input.lib.eval;

import javax.xml.bind.annotation.XmlAttribute;

public class Relation {

	private String name;
	private String id;
	private String from;
	private String to;
	private String negation;
	
	public Relation() { }
	
	public Relation(String name, String id, String from, String to,
			String negation) {
		super();
		this.name = name;
		this.id = id;
		this.from = from;
		this.to = to;
		this.negation = negation;
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id " + id + "\n");
		sb.append("name " + name + "\n");
		sb.append("from " + from + "\n");
		sb.append("to " + to + "\n");
		sb.append("negation " + negation + "\n");
		return sb.toString();
	}
}


