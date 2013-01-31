package semanticMarkup.io.output.lib.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class XMLVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public XMLVolumeWriter(@Named("Run_OutFile") String filePath) {
		super(filePath);
	}
	
	@Override
	public void write(List<Treatment> treatments) throws Exception {
		FileOutputStream outputStream = new FileOutputStream(new File(filePath + ".xml"));
		
		XMLTreatmentContainer treatmentContainer = new XMLTreatmentContainer(treatments);
		
		Class<?>[] classes = { ContainerTreatmentElement.class, Treatment.class, TreatmentElement.class, ValueTreatmentElement.class, XMLTreatmentContainer.class };
		JAXBContext jc = JAXBContext.newInstance(classes);	    
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(treatmentContainer, outputStream);
	}
	
	public static void main(String[] args) throws Exception {
		String filePath = "test";
		
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment1 = new Treatment("treatment1");
		Treatment treatment2 = new Treatment("treatment2");
		Treatment treatment3 = new Treatment("treatment3");
		treatments.add(treatment1);
		treatments.add(treatment2);
		treatments.add(treatment3);
		treatment1.addTreatmentElement(new ValueTreatmentElement("habitat", "value"));
		treatment1.addTreatmentElement(new ValueTreatmentElement("author", "value2"));
		treatment1.addTreatmentElement(new ValueTreatmentElement("description", "text"));
		ContainerTreatmentElement references = new ContainerTreatmentElement("references");
		references.addTreatmentElement(new ValueTreatmentElement("ref1", "http://..."));
		references.addTreatmentElement(new ValueTreatmentElement("ref2", "http://2..."));
		treatment1.addTreatmentElement(references);
		
		FileOutputStream outputStream = new FileOutputStream(new File(filePath));
		
		XMLTreatmentContainer treatmentContainer = new XMLTreatmentContainer(treatments);
		
		Class<?>[] classes = { ContainerTreatmentElement.class, Treatment.class, TreatmentElement.class, ValueTreatmentElement.class, XMLTreatmentContainer.class };
		JAXBContext jc = JAXBContext.newInstance(classes);
		//JAXBContext jc = JAXBContext.newInstance("semanticMarkup.core");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(treatmentContainer, outputStream);
	}
}
