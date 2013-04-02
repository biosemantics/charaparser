package semanticMarkup.io.input.lib.eval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Description serves as data container for a description for JAXB  
 * @author rodenhausen
 */
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
}
