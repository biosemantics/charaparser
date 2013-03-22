package semanticMarkup.core;

public class Treatment extends ContainerTreatmentElement {

	// for JAXB
	public Treatment() {}
	
	public Treatment(String treatmentName) {
		//super("treatment_" + treatmentName);
		super(treatmentName);
	}
}
