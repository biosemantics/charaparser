package semanticMarkup.core;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

public class ValueTreatmentElement extends TreatmentElement {

	protected String value;
	
	//for JAXB
	public ValueTreatmentElement() { }
	
	public ValueTreatmentElement(String name, String value) {
		super(name);
		this.value = value;
	}

	@XmlElement(name="value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override 
	public Object clone() {
		ValueTreatmentElement clone = (ValueTreatmentElement)super.clone();
		clone.value = new String(value);
		return clone;
	}
}
