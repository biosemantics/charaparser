package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class Processor extends Element {

	private String date;
	private Software software;
	private String operator;
	private Resource resource;

	public Processor(String date, Software software, String operator,
			Resource resource) {
		super();
		this.date = date;
		this.software = software;
		this.operator = operator;
		this.resource = resource;
	}
	
	public Processor() { }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Software getSoftware() {
		return software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public void removeElementRecursively(Element element) {
		if(software.equals(element))
			software = null;
		if(resource.equals(element))
			resource = null;
		software.removeElementRecursively(element);
		resource.removeElementRecursively(element);
	}
}

