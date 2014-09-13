package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.StatementAttribute;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Value;
import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class Statement extends Element {
	
	@XmlPath("@" + StatementAttribute.text)
	private String text;
	
	@XmlPath("@" + StatementAttribute.id)
	private String id;

	private List<Value> values = new LinkedList<Value>();
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public void addValue(Value value){
		values.add(value);
	}
	
	public void setValues(List<Value> values){
		this.values = values;
	}
	
	public List<Value> getValues(){
		return this.values;
	}
	
	@Override
	public void removeElementRecursively(Element element) {
		Iterator<Value> valueIterator = values.iterator();
		while(valueIterator.hasNext()) {
			Value value = valueIterator.next();
			if(value.equals(element))
				valueIterator.remove();
			else
				value.removeElementRecursively(element);
		}
	}
}
