package semanticMarkup.io.output.lib.xml2;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

/**
 * Description serves as data description for a key for JAXB  
 * @author rodenhausen
 */
public class Description {

	private ArrayList<Statement> statement = new ArrayList<Statement>();
	
	public Description() { }

	public Description(ArrayList<Statement> statement) {
		super();
		this.statement = statement;
	}

	@XmlElement
	public ArrayList<Statement> getStatement() {
		return statement;
	}

	public void setStatement(ArrayList<Statement> statement) {
		this.statement = statement;
	}
	
}
