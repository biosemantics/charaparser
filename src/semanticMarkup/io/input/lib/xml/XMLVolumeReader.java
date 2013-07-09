package semanticMarkup.io.input.lib.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.model.ContainerTreatmentElement;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.ValueTreatmentElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * XMLVolumeReader reads a list of treatments of type 2 that are valid against the XML Schema resources/io/FNAXMLSchemaInput.xml
 * @author rodenhausen
 */
public class XMLVolumeReader extends AbstractFileVolumeReader {

	/**
	 * @param sourceDirectory
	 */
	@Inject
	public XMLVolumeReader(@Named("XMLVolumeReader_SourceDirectory")String sourceDirectory) {
		super(sourceDirectory);
	}

	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> result = new ArrayList<Treatment>();
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.input.lib.xml");
        Unmarshaller u = jc.createUnmarshaller();
        
        File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			File file = files[i];
	        semanticMarkup.io.input.lib.xml.Treatment xmlTreatment = (semanticMarkup.io.input.lib.xml.Treatment)u.unmarshal(file);
	        Treatment treatment = transformTreatment(xmlTreatment);
	        treatment.setName(file.getName().split("\\.(?=[^\\.]+$)")[0]);
	        result.add(treatment);
		}
        return result;
	}

	private Treatment transformTreatment(semanticMarkup.io.input.lib.xml.Treatment xmlTreatment) {
        Treatment treatment = new Treatment();
        treatment.addTreatmentElement(new ValueTreatmentElement("number", xmlTreatment.getNumber()));

        List<Object> taxonIdentificationOrEtymologyOrOtherInfo = xmlTreatment.getTaxonIdentificationOrEtymologyOrOtherInfo();
        List<Object> referencesOrKey = xmlTreatment.getReferencesOrKey();
        for(Object item : taxonIdentificationOrEtymologyOrOtherInfo) {
        	if(item instanceof JAXBElement) {
        		JAXBElement<String> element = (JAXBElement<String>) item;
        		treatment.addTreatmentElement(new ValueTreatmentElement(element.getName().toString(), element.getValue()));
        	}
        	if(item instanceof TaxonIdentification) {
        		TaxonIdentification taxonIdentification = (TaxonIdentification)item;
        		ContainerTreatmentElement taxonIdentificationElement = new ContainerTreatmentElement("TaxonIdentification");
        		treatment.addTreatmentElement(taxonIdentificationElement);
        		List<Object> taxonIdentificationContent = taxonIdentification.getContent();
        		
        		for(Object content : taxonIdentificationContent) {
        			if(content instanceof JAXBElement) {
        				JAXBElement<String> element = (JAXBElement<String>) content;
        				taxonIdentificationElement.addTreatmentElement(new ValueTreatmentElement(element.getName().toString(), element.getValue()));
        			}
        			if(content instanceof PlaceOfPublication) {
        				PlaceOfPublication placeOfPublication = (PlaceOfPublication)content;
        				ContainerTreatmentElement placeOfPublicationElement = new ContainerTreatmentElement("place_of_publication");
        				taxonIdentificationElement.addTreatmentElement(placeOfPublicationElement);
        				placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("place_in_publication", placeOfPublication.getPlaceInPublication()));
        				placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("publication_title", placeOfPublication.getPublicationTitle()));
        				for(String otherInfo : placeOfPublication.getOtherInfo()) 
        					placeOfPublicationElement.addTreatmentElement(new ValueTreatmentElement("other_info", otherInfo));
        			}
        		}
        		
        		taxonIdentificationElement.setAttribute("Status", taxonIdentification.getStatus());
        	}
        }
        
        for(Object item : referencesOrKey) {
        	if(item instanceof Key) {
        		ContainerTreatmentElement keyElement = new ContainerTreatmentElement("key");
        		treatment.addTreatmentElement(keyElement);
        		Key key = (Key)item;
        		keyElement.addTreatmentElement(new ValueTreatmentElement("key_heading", key.getKeyHeading()));
        		for(String author : key.getKeyAuthor())
        			keyElement.addTreatmentElement(new ValueTreatmentElement("author", author));
        		
        		List<Object> keyDiscussionOrKeyHeadOrKeyStatements = key.getKeyDiscussionOrKeyHeadOrKeyStatement();
        		for(Object keyDiscussionOrKeyHeadOrKeyStatement : keyDiscussionOrKeyHeadOrKeyStatements) {
        			if(keyDiscussionOrKeyHeadOrKeyStatement instanceof JAXBElement) {
        				JAXBElement<String> element = (JAXBElement<String>)keyDiscussionOrKeyHeadOrKeyStatement;
        				keyElement.addTreatmentElement(new ValueTreatmentElement(element.getName().toString(), element.getValue()));
        			}
        			if(keyDiscussionOrKeyHeadOrKeyStatement instanceof KeyStatement) {
        				KeyStatement keyStatement = (KeyStatement)keyDiscussionOrKeyHeadOrKeyStatement;
        				ContainerTreatmentElement keyStatementElement = new ContainerTreatmentElement("key_statement");
        				keyElement.addTreatmentElement(keyStatementElement);
        				keyStatementElement.addTreatmentElement(new ValueTreatmentElement("determination", keyStatement.getDetermination()));
        				keyStatementElement.addTreatmentElement(new ValueTreatmentElement("next_statement_id", keyStatement.getNextStatementId()));
        				keyStatementElement.addTreatmentElement(new ValueTreatmentElement("statement", keyStatement.getStatement()));
        				keyStatementElement.addTreatmentElement(new ValueTreatmentElement("statement_id", keyStatement.getStatementId()));
        			}
        		}
        	}
        	if(item instanceof References) {
        		References references = (References)item;
        		ContainerTreatmentElement referencesElement = new ContainerTreatmentElement("references");
        		treatment.addTreatmentElement(referencesElement);
        		referencesElement.setAttribute("heading", references.getHeading());
        		for(String reference : references.getReference()) {
        			referencesElement.addTreatmentElement(new ValueTreatmentElement("reference", reference));
        		}
        		for(String referenceDiscussion : references.getReferenceDiscussion()) {
        			referencesElement.addTreatmentElement(new ValueTreatmentElement("reference_discussion", referenceDiscussion));	
        		}
        	}
        }
        return treatment;
	}
}
