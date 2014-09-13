package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.model.Element;



public class Description extends Element {

	private String text;
	//private DescriptionsFile descriptionsFile;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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
	/*@Override
	public Object clone(){
		Description d = new Description();
		d.setText(d.getText());
		LinkedList<Statement> copy = new LinkedList<Statement>();
		Iterator<Statement> it = statements.iterator();
		while(it.hasNext()){
			Statement state = it.next();
			copy.add((Statement)state.clone());
		}
		d.setStatements(copy);
		return d;
	}*/
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
