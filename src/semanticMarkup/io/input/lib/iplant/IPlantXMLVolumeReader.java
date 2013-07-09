package semanticMarkup.io.input.lib.iplant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.model.ContainerTreatmentElement;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.ValueTreatmentElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class IPlantXMLVolumeReader extends AbstractFileVolumeReader  {

	@Inject
	public IPlantXMLVolumeReader(@Named("IPlantXMLVolumeReader_Source") String filePath) {
		super(filePath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> result = new ArrayList<Treatment>();
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.input.lib.iplant");
        Unmarshaller u = jc.createUnmarshaller();
        
        File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			File file = files[i];
	        semanticMarkup.io.input.lib.iplant.Treatment xmlTreatment = (semanticMarkup.io.input.lib.iplant.Treatment)u.unmarshal(file);
	        Treatment treatment = transformTreatment(xmlTreatment);
	        treatment.setName(file.getName().split("\\.(?=[^\\.]+$)")[0]);
	        result.add(treatment);
		}
        return result;
	}

	private Treatment transformTreatment(semanticMarkup.io.input.lib.iplant.Treatment xmlTreatment) {
		Treatment treatment = new Treatment();
	
		ContainerTreatmentElement metaTreatmentElement = new ContainerTreatmentElement("meta");
		treatment.addTreatmentElement(metaTreatmentElement);
		Meta meta = xmlTreatment.getMeta();
		String source = meta.getSource();
		metaTreatmentElement.addTreatmentElement(new ValueTreatmentElement("source", source));
		List<String> otherInfos = meta.getOtherInfo();
		for(String otherInfo : otherInfos) {
			metaTreatmentElement.addTreatmentElement(new ValueTreatmentElement("other_info", otherInfo));
		}
		
		ContainerTreatmentElement taxonIdentificationTreatmetnElement = new ContainerTreatmentElement("TaxonIdentification");
		treatment.addTreatmentElement(taxonIdentificationTreatmetnElement);
		TaxonIdentification taxonIdentification = xmlTreatment.getTaxonIdentification();
		String taxonName = taxonIdentification.getTaxonName();
		taxonIdentificationTreatmetnElement.addTreatmentElement(new ValueTreatmentElement("taxon_name", source));
		otherInfos = taxonIdentification.getOtherInfo();
		for(String otherInfo : otherInfos) {
			taxonIdentificationTreatmetnElement.addTreatmentElement(new ValueTreatmentElement("other_info", otherInfo));
		}
		
		treatment.addTreatmentElement(new ValueTreatmentElement("description", xmlTreatment.getDescription()));
		
		List<String> discussions = xmlTreatment.getDiscussion();
		for(String discussion : discussions) {
			treatment.addTreatmentElement(new ValueTreatmentElement("discussion", discussion));
		}
		return treatment;
	}

}
