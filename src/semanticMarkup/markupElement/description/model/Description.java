package semanticMarkup.markupElement.description.model;


import java.util.LinkedList;
import java.util.List;

import semanticMarkup.model.Element;


public class Description extends Element {

	private String type; //as requested per Dongye
	private String text;
	//private DescriptionsFile descriptionsFile;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/*
	public DescriptionsFile getDescriptionsFile() {
		return descriptionsFile;
	}

	public void setDescriptionsFile(DescriptionsFile descriptionsFile) {
		this.descriptionsFile = descriptionsFile;
	}*/



	// inplace modification, later marshalling out of DescriptionFile again
	private List<Statement> statements = new LinkedList<Statement>();

	public List<Statement> getStatements() {
		return statements;
	}

	public void setStatements(List<Statement> statements) {
		this.statements = statements;
	}

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}
}
