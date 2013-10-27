package semanticMarkup.io.input.lib.newIPlant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.google.inject.name.Named;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.AbstractFileVolumeReader;

public class NewIPlantXMLVolumeReader extends AbstractFileVolumeReader{

	public NewIPlantXMLVolumeReader(@Named("IPlantXMLVolumeReader_Source") String filePath) {
		super(filePath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> result = new ArrayList<Treatment>();
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.input.lib.newIplant");
        Unmarshaller u = jc.createUnmarshaller();
        
        File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			File file = files[i];
	        InputStream inputStream = new FileInputStream(file);
	        Reader reader = new InputStreamReader(inputStream, "UTF-8");
	        semanticMarkup.io.input.lib.newIPlant.Treatment xmlTreatment = (semanticMarkup.io.input.lib.newIPlant.Treatment)u.unmarshal(reader);
	        Treatment treatment = transformTreatment(xmlTreatment);
	        treatment.setName(file.getName().split("\\.(?=[^\\.]+$)")[0]);
	        result.add(treatment);
		}
        return result;
	}

	private Treatment transformTreatment(semanticMarkup.io.input.lib.newIPlant.Treatment xmlTreatment) {
		Treatment treatment = new Treatment();
		
		//meta
		ContainerTreatmentElement metaTreatmentElement = new ContainerTreatmentElement("meta");
		Meta meta = xmlTreatment.getMeta();
		String source = meta.getSource();
		List<String> otherInfosOnMeta = meta.getOtherInfoOnMeta();
		ProcessedBy processedBy = meta.getProcessedBy();
		metaTreatmentElement.addTreatmentElement(new ValueTreatmentElement("source", source));
		
		ContainerTreatmentElement processedByContainerTreatmentElement = new ContainerTreatmentElement("processed_by");
		for(Object object : processedBy.getProcessorOrCharaparser()) {
			if(object instanceof Processor) {
				Processor processor = (Processor)object;
				ValueTreatmentElement processorTreatmentElement = new ValueTreatmentElement("processor", processor.getValue());
				processorTreatmentElement.setAttribute("process_type", processor.getProcessType());
				processedByContainerTreatmentElement.addTreatmentElement(processorTreatmentElement);
			}
			if(object instanceof Charaparser) {
				Charaparser charaparser = (Charaparser)object;
				ContainerTreatmentElement charaparserTreatmentElement = new ContainerTreatmentElement("charaparser");
				charaparserTreatmentElement.addTreatmentElement(new ValueTreatmentElement("charaparser_version", charaparser.getCharaparserVersion()));
				charaparserTreatmentElement.addTreatmentElement(new ValueTreatmentElement("charaparser_user", charaparser.getCharaparserUser()));
				charaparserTreatmentElement.addTreatmentElement(new ValueTreatmentElement("glossary_name", charaparser.getGlossaryName()));
				charaparserTreatmentElement.addTreatmentElement(new ValueTreatmentElement("glossary_version", charaparser.getGlossaryVersion()));
				processedByContainerTreatmentElement.addTreatmentElement(charaparserTreatmentElement);
			}
		}
		metaTreatmentElement.addTreatmentElement(processedByContainerTreatmentElement);

		for(String otherInfoOnMeta : otherInfosOnMeta)
			metaTreatmentElement.addTreatmentElement(new ValueTreatmentElement("other_info_on_meta", otherInfoOnMeta));
		treatment.addTreatmentElement(metaTreatmentElement);

		//taxon identification
		TaxonIdentification taxonIdentification = xmlTreatment.getTaxonIdentification();
		ContainerTreatmentElement taxonIdentificationElement = new ContainerTreatmentElement("taxon_identification");
		for(Object content : taxonIdentification.getContent()) {
			if(content instanceof PlaceOfPublication) {
				PlaceOfPublication placeOfPublicationContent = (PlaceOfPublication)content;
				ContainerTreatmentElement placeOfPublicationElement = new ContainerTreatmentElement("place_of_publication");
				placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("publication_title", placeOfPublicationContent.getPublicationTitle()));
				placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("place_in_publication", placeOfPublicationContent.getPlaceInPublication()));
				for(String otherInfoPub : placeOfPublicationContent.getOtherInfoOnPub()) 
					placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("other_info_on_pub", otherInfoPub));
				taxonIdentificationElement.addTreatmentElement(placeOfPublicationElement);
			} else if(content instanceof JAXBElement) {
        		JAXBElement<String> element = (JAXBElement<String>) content;
        		taxonIdentificationElement.addTreatmentElement(new ValueTreatmentElement(element.getName().toString(), element.getValue()));
        	}
		}
		taxonIdentificationElement.setAttribute("status", taxonIdentification.getStatus());
		treatment.addTreatmentElement(taxonIdentificationElement);
		
		//description
		treatment.addTreatmentElement(new ValueTreatmentElement("description", xmlTreatment.getDescription()));
		
		//discussion
		for(String discussion : xmlTreatment.getDiscussion()) {
			treatment.addTreatmentElement(new ValueTreatmentElement("discussion", discussion));
		}
		
		return treatment;
	}

}
