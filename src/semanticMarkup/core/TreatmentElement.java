package semanticMarkup.core;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;

import semanticMarkup.log.LogLevel;

/**
 * A TreatmentElement applies together with the ContainerTreatmentElement and ValueTreatmentElement a composite pattern.
 * @author rodenhausen
 */
public class TreatmentElement implements Cloneable {

	protected String name;
	protected HashMap<String, String> attributes = new HashMap<String, String>();
	
	/**
	 * JAXB needs a non-argument constructor
	 */
	public TreatmentElement(){}
	
	/**
	 * @param name
	 */
	public TreatmentElement(String name) {
		this.name = name;
	}

	/**
	 * @return name
	 */
	@XmlElement(name="name")
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the attributes
	 */
	public HashMap<String, String> getAttributes() {
		return this.attributes;
	}
	
	/**
	 * @param name
	 * @return if this contains an attribute of name
	 */
	public boolean containsAttribute(String name) {
		return this.attributes.containsKey(name);
	}
	
	/**
	 * set the attribute of name with value
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, String value) {
		this.attributes.put(name, value);
	}
	
	/**
	 * @param name
	 * @return the attribute value of name
	 */
	public String getAttribute(String name) {
		return this.attributes.get(name.trim());
	}
	
	/**
	 * append to the current value of the attribute with name the given value separated by comma
	 * @param name
	 * @param value
	 */
	public void appendAttribute(String name, String value) {
		String newValue = "";
		if(this.attributes.containsKey(name)) {
			newValue = this.attributes.get(name) + ", " + value;
		} else {
			newValue = value;
		}
		this.attributes.put(name, newValue);
	}
	
	/**
	 * remove the attribute with the given name
	 * @param name
	 */
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}
	
	@Override
	public Object clone() {
		try {
			TreatmentElement clone = (TreatmentElement)super.clone();
			clone.setName(new String(name));
			clone.attributes = new HashMap<String, String>();
			for(Entry<String, String> entry : this.attributes.entrySet()) {
				clone.attributes.put(entry.getKey(), entry.getValue());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, e);
		}
		return null;
	}
}
