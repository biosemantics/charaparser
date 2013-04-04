package semanticMarkup.core.description;

import java.util.HashMap;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import semanticMarkup.core.ContainerTreatmentElement;

/**
 * A DescriptionTreatmentElemetn has a DescriptionTreatmentElementType and can have a number of attributes that have a name and value
 * @author rodenhausen
 */
public class DescriptionTreatmentElement extends ContainerTreatmentElement {

	@JsonIgnore
	private DescriptionTreatmentElementType descriptionType; //enum and used for name constructor
	private HashMap<String, String> attributes = new HashMap<String, String>();
	
	/**
	 * JAXB needs a non-argument constructor
	 */
	public DescriptionTreatmentElement() { }
	
	/**
	 * @param descriptionType
	 */
	public DescriptionTreatmentElement(DescriptionTreatmentElementType descriptionType) {
		super(descriptionType.toString());
		this.descriptionType = descriptionType;
	}
	
	/**
	 * @return the attributes
	 */
	public HashMap<String, String> getAttributes() {
		return this.attributes;
	}
	
	/**
	 * @param descriptionType
	 * @return if this is of descriptionType
	 */
	public boolean isOfDescriptionType(DescriptionTreatmentElementType descriptionType) {
		return this.descriptionType.equals(descriptionType);
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
	 * @return the DescriptionTreatmentElementType
	 */
	public DescriptionTreatmentElementType getDescriptionType() {
		return descriptionType;
	}

	/**
	 * @param descriptionTreatmentElementType to set
	 */
	public void setDescriptionType(DescriptionTreatmentElementType descriptionTreatmentElementType) {
		this.descriptionType = descriptionType;
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
		DescriptionTreatmentElement clone = (DescriptionTreatmentElement)super.clone();
		clone.descriptionType = this.getDescriptionType();
		clone.attributes = new HashMap<String, String>();
		for(Entry<String, String> entry : this.attributes.entrySet()) {
			clone.attributes.put(entry.getKey(), entry.getValue());
		}
		return clone;
	}
}
