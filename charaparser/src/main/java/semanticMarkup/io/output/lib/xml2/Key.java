package semanticMarkup.io.output.lib.xml2;

import javax.xml.bind.annotation.XmlElement;

public class Key {

	private String statement_id;
	private String statement;
	private String next_statement;
	
	private Key() { }
	
	public Key(String statement_id, String statement, String next_statement) {
		super();
		this.statement_id = statement_id;
		this.statement = statement;
		this.next_statement = next_statement;
	}

	@XmlElement
	public String getStatement_id() {
		return statement_id;
	}

	public void setStatement_id(String statement_id) {
		this.statement_id = statement_id;
	}

	@XmlElement
	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	@XmlElement
	public String getNext_statement() {
		return next_statement;
	}

	public void setNext_statement(String next_statement) {
		this.next_statement = next_statement;
	}	
}
