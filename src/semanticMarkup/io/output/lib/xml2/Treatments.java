package semanticMarkup.io.output.lib.xml2;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://some/namespace")
@XmlRootElement
public class Treatments {

	private ArrayList<Treatment> treatments = new ArrayList<Treatment>();
	
	public Treatments() {
		
	}
	
	public Treatments(ArrayList<Treatment> treatments) {
		this.treatments = treatments;
	}

	@XmlElement(name="treatment")
	public ArrayList<Treatment> getTreatment() {
		return treatments;
	}

	public void setTreatment(ArrayList<Treatment> treatments) {
		this.treatments = treatments;
	}
}
