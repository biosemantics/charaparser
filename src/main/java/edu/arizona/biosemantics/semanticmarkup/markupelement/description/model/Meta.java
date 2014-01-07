package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class Meta extends Element {
	
	private String source;
	private List<Object> processedBy = new LinkedList<Object>();
	private List<String> otherInfoOnMeta = new LinkedList<String>();

	public void addOtherInfoOnMeta(String otherInfoOnMeta) {
		this.otherInfoOnMeta.add(otherInfoOnMeta);
	}
	
	public void addProcessedBy(Object processedBy) {
		this.processedBy.add(processedBy);
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<Object> getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(List<Object> processedBy) {
		this.processedBy = processedBy;
	}

	public List<String> getOtherInfoOnMeta() {
		return otherInfoOnMeta;
	}
	
	public void setOtherInfoOnMeta(List<String> otherInfoOnMeta) {
		this.otherInfoOnMeta = otherInfoOnMeta;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}
}