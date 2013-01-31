package semanticMarkup.io.input.lib.eval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="description")
public class Description {

	private Statement statement;
	
	public Description() { }

	@XmlElement
	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	} 
	
	public String toString() {
		return statement.toString();
	}
}
