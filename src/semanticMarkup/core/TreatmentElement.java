package semanticMarkup.core;

import javax.xml.bind.annotation.XmlElement;

import semanticMarkup.log.LogLevel;

/**
 * A TreatmentElement applies together with the ContainerTreatmentElement and ValueTreatmentElement a composite pattern.
 * @author rodenhausen
 */
public class TreatmentElement implements Cloneable {

	protected String name;
	
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
	
	@Override
	public Object clone() {
		try {
			TreatmentElement clone = (TreatmentElement)super.clone();
			clone.setName(new String(name));
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, e);
		}
		return null;
	}
}
