package semanticMarkup.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * A ValueTreatmentElement has a name and a value
 * @author rodenhausen
 */
public class ValueTreatmentElement extends TreatmentElement {

	protected String value;
	
	/**
	 * JAXB needs a non-argument constructor
	 */
	public ValueTreatmentElement() { }
	
	/**
	 * @param name
	 * @param value
	 */
	public ValueTreatmentElement(String name, String value) {
		super(name);
		this.value = value;
	}

	/**
	 * @return the value
	 */
	@XmlElement(name="value")
	public String getValue() {
		return value;
	}

	/**
	 * set the value
	 * @param value
	 */
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
