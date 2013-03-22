package semanticMarkup.core.description;

import java.util.HashMap;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import semanticMarkup.core.ContainerTreatmentElement;

public class DescriptionTreatmentElement extends ContainerTreatmentElement {

	@JsonIgnore
	private DescriptionType descriptionType; //enum and used for name constructor
	private HashMap<String, String> properties = new HashMap<String, String>();
	
	public DescriptionTreatmentElement() {
		
	}
	
	public DescriptionTreatmentElement(DescriptionType descriptionType) {
		super(descriptionType.toString());
		this.descriptionType = descriptionType;
	}
	
	public HashMap<String, String> getProperties() {
		return this.properties;
	}
	
	public boolean isOfDescriptionType(DescriptionType descriptionType) {
		return this.descriptionType.equals(descriptionType);
	}
	
	public boolean containsProperty(String key) {
		return this.properties.containsKey(key);
	}
	
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return this.properties.get(key.trim());
	}
	
	public void appendProperty(String key, String value) {
		String newValue = "";
		if(this.properties.containsKey(key)) {
			newValue = this.properties.get(key) + ", " + value;
		} else {
			newValue = value;
		}
		this.properties.put(key, newValue);
	}

	public DescriptionType getDescriptionType() {
		return descriptionType;
	}

	public void setDescriptionType(DescriptionType descriptionType) {
		this.descriptionType = descriptionType;
	}
	
	public void removeProperty(String key) {
		this.properties.remove(key);
	}
	
	@Override 
	public Object clone() {
		DescriptionTreatmentElement clone = (DescriptionTreatmentElement)super.clone();
		clone.descriptionType = this.getDescriptionType();
		clone.properties = new HashMap<String, String>();
		for(Entry<String, String> entry : this.properties.entrySet()) {
			clone.properties.put(entry.getKey(), entry.getValue());
		}
		return clone;
	}
}
