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
	private DescriptionTreatmentElementType descriptionTreatmentElementType; //enum and used for name constructor
	
	/**
	 * JAXB needs a non-argument constructor
	 */
	public DescriptionTreatmentElement() { }
	
	/**
	 * @param descriptionType
	 */
	public DescriptionTreatmentElement(DescriptionTreatmentElementType descriptionTreatmentElementType) {
		super(descriptionTreatmentElementType.toString());
		this.descriptionTreatmentElementType = descriptionTreatmentElementType;
	}
	
	/**
	 * @param descriptionType
	 * @return if this is of descriptionType
	 */
	public boolean isOfDescriptionType(DescriptionTreatmentElementType descriptionType) {
		return this.descriptionTreatmentElementType.equals(descriptionType);
	}

	/**
	 * @return the DescriptionTreatmentElementType
	 */
	public DescriptionTreatmentElementType getDescriptionTreatmentElementType() {
		return descriptionTreatmentElementType;
	}

	/**
	 * @param descriptionTreatmentElementType to set
	 */
	public void setDescriptionType(DescriptionTreatmentElementType descriptionTreatmentElementType) {
		this.descriptionTreatmentElementType = descriptionTreatmentElementType;
	}
	
	@Override 
	public Object clone() {
		DescriptionTreatmentElement clone = (DescriptionTreatmentElement)super.clone();
		clone.descriptionTreatmentElementType = this.getDescriptionTreatmentElementType();
		return clone;
	}
}
