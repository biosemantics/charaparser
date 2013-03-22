package semanticMarkup.core;

import javax.xml.bind.annotation.XmlElement;

import semanticMarkup.log.LogLevel;

public class TreatmentElement implements Cloneable {

	protected String name;
	
	public TreatmentElement(){}
	
	public TreatmentElement(String name) {
		this.name = name;
	}

	@XmlElement(name="name")
	public String getName() {
		return name;
	}

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
