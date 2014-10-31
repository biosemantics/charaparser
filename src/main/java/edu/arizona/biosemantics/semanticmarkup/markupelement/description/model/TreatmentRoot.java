package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import java.util.LinkedList;
import java.util.List;

public class TreatmentRoot {

	private List<Description> descriptionDescriptions = new LinkedList<Description>();
	private List<Description> keyStatementDescriptions = new LinkedList<Description>();
	
	public List<Description> getDescriptions() {
		List<Description> allDescriptions = new LinkedList<Description>();
		allDescriptions.addAll(descriptionDescriptions);
		allDescriptions.addAll(keyStatementDescriptions);
		return allDescriptions;
	}

	public List<Description> getDescriptionDescriptions() {
		return descriptionDescriptions;
	}

	public void setDescriptionDescriptions(List<Description> descriptionDescriptions) {
		this.descriptionDescriptions = descriptionDescriptions;
	}

	public List<Description> getKeyStatementDescriptions() {
		return keyStatementDescriptions;
	}

	public void setKeyStatementDescriptions(
			List<Description> keyStatementDescriptions) {
		this.keyStatementDescriptions = keyStatementDescriptions;
	}



}
