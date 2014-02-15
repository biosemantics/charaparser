package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class Elevation extends Element {
	
	private String text;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

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

	@Override
	public void removeElementRecursively(Element element) {
		Iterator<Statement> statementsIterator = statements.iterator();
		while(statementsIterator.hasNext()) {
			Statement statement = statementsIterator.next();
			if(statement.equals(element))
				statementsIterator.remove();
			else
				statement.removeElementRecursively(element);
		}
	}
}
