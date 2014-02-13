package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class Meta extends Element {
	
	private Source source;
	private List<Processor> processedBy = new LinkedList<Processor>();
	private List<OtherInfoOnMeta> otherInfoOnMeta = new LinkedList<OtherInfoOnMeta>();

	public void addOtherInfoOnMeta(OtherInfoOnMeta otherInfoOnMeta) {
		this.otherInfoOnMeta.add(otherInfoOnMeta);
	}
	
	public void addProcessor(Processor processor) {
		this.processedBy.add(processor);
	}
	
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public List<Processor> getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(List<Processor> processedBy) {
		this.processedBy = processedBy;
	}

	public List<OtherInfoOnMeta> getOtherInfoOnMeta() {
		return otherInfoOnMeta;
	}
	
	public void setOtherInfoOnMeta(List<OtherInfoOnMeta> otherInfoOnMeta) {
		this.otherInfoOnMeta = otherInfoOnMeta;
	}

	@Override
	public void removeElementRecursively(Element element) {
		if(source.equals(element))
			source = null;
		Iterator<Processor> processorIterator = processedBy.iterator();
		while(processorIterator.hasNext()) {
			Processor processor = processorIterator.next();
			if(processor.equals(element))
				processorIterator.remove();
		}
		
		Iterator<OtherInfoOnMeta> otherInfoOnMetaIterator = otherInfoOnMeta.iterator();
		while(otherInfoOnMetaIterator.hasNext()) {
			OtherInfoOnMeta otherInfoOnMeta = otherInfoOnMetaIterator.next();
			if(otherInfoOnMeta.equals(element))
				otherInfoOnMetaIterator.remove();
		}
	}
}