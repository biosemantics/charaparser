package semanticMarkup.io.output.lib.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import semanticMarkup.core.Treatment;

/**
 * XMLTreatmentContainer contains a list of treatments and defines the root element for JAXB
 * @author rodenhausen
 */
@XmlType(namespace = "http://some/namespace")
@XmlRootElement(name="treatments")
public class XMLTreatmentContainer {

	private ArrayList<Treatment> treatments;
	
	public XMLTreatmentContainer() {
		
	}
	
	public XMLTreatmentContainer(List<Treatment> treatments) {
		this.treatments = new ArrayList<Treatment>();
		this.treatments.addAll(treatments);
	}

	@XmlElement(name="treatment")
	public ArrayList<Treatment> getTreatments() {
		return treatments;
	}

	public void setTreatments(List<Treatment> treatments) {
		this.treatments = new ArrayList<Treatment>();
		this.treatments.addAll(treatments);
	}
}
