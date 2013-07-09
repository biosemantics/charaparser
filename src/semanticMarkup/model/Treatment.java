package semanticMarkup.model;

/**
 * A treatments has a name and contains other TreatmentElements, e.g. a DescriptionTreatmentElement
 * @author rodenhausen
 */
public class Treatment extends ContainerTreatmentElement {

	/**
	 * JAXB needs a non-argument constructor
	 */
	public Treatment() {}
	
	/**
	 * @param treatmentName
	 */
	public Treatment(String treatmentName) {
		//super("treatment_" + treatmentName);
		super(treatmentName);
	}
}
