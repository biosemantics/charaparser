package semanticMarkup.io.input.transform;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.io.input.lib.iplant.Meta;
import semanticMarkup.io.input.lib.iplant.Treatment;
import semanticMarkup.io.input.lib.xml.Key;
import semanticMarkup.io.input.lib.xml.KeyStatement;
import semanticMarkup.io.input.lib.xml.PlaceOfPublication;
import semanticMarkup.io.input.lib.xml.References;
import semanticMarkup.io.input.lib.xml.TaxonIdentification;

public class FNAXMLToIPlantXMLTransformer {

	private String inPath;
	private String outPath;
	
	public FNAXMLToIPlantXMLTransformer(String inPath, String outPath) {
		super();
		this.inPath = inPath;
		this.outPath = outPath;
	}

	public void translate() throws Exception {
		JAXBContext xmlContext = JAXBContext.newInstance("semanticMarkup.io.input.lib.xml");
        Unmarshaller xmlUnmarshaller = xmlContext.createUnmarshaller();
        
        JAXBContext iplantContext = JAXBContext.newInstance("semanticMarkup.io.input.lib.iplant");
		Marshaller iplantMarshaller = iplantContext.createMarshaller();
		iplantMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        File sourceDirectory = new File(inPath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			File file = files[i];
	        
			semanticMarkup.io.input.lib.xml.Treatment xmlTreatment = (semanticMarkup.io.input.lib.xml.Treatment)xmlUnmarshaller.unmarshal(file);
	        semanticMarkup.io.input.lib.iplant.Treatment outTreatment = doTranslation(xmlTreatment);
	        		
	        File outFile = new File(outPath + File.separator + file.getName());
	        outFile.getParentFile().mkdirs();
	        FileOutputStream outputStream = new FileOutputStream(outFile);
			iplantMarshaller.marshal(outTreatment, outputStream);
		}
		
		
		
		/*
		XMLVolumeReader reader = new XMLVolumeReader(inFile);
		
		List<Treatment> treatments = reader.read();
		
		List<Treatment> result = new LinkedList<Treatment>();
		for(Treatment treatment : treatments) {
			Treatment newTreatment = new Treatment();
			
			ValueTreatmentElement numberElement = treatment.getValueTreatmentElement("number");
			
			String source = numberElement == null ? null : treatment.getValueTreatmentElement("number").getValue();
			
			String taxonName = null;
			ContainerTreatmentElement taxonIdentificationElement = treatment.getContainerTreatmentElement("TaxonIdentification");
			if(taxonIdentificationElement != null) {
				ValueTreatmentElement genusElement = taxonIdentificationElement.getValueTreatmentElement("genus_name");
				taxonName = genusElement == null ? null : genusElement.getValue();
			}
			
			ValueTreatmentElement descriptionElement = treatment.getValueTreatmentElement("description");
			String description = descriptionElement == null ? null : descriptionElement.getValue();
			ValueTreatmentElement discussionElement = treatment.getValueTreatmentElement("discussion");
			String discussion = discussionElement == null ? null : discussionElement.getValue();
			
			
			ContainerTreatmentElement metaElement = new ContainerTreatmentElement("meta");
			if(source != null) {
				metaElement.addTreatmentElement(new ValueTreatmentElement("source", source));
				newTreatment.addTreatmentElement(metaElement);
			}
			
			ContainerTreatmentElement newTaxonIdentificationElement = new ContainerTreatmentElement("TaxonIdentification");
			if(taxonName != null) {
				newTaxonIdentificationElement.addTreatmentElement(new ValueTreatmentElement("taxon_name", taxonName));
				newTreatment.addTreatmentElement(newTaxonIdentificationElement);
			}
			if(description != null) 
				newTreatment.addTreatmentElement(new ValueTreatmentElement("description", description));
			
			if(discussion != null)
				newTreatment.addTreatmentElement(new ValueTreatmentElement("discussion", discussion));
			
			result.add(newTreatment);
			
			System.out.println(newTreatment.toString());
		}
		
		IPlantXMLVolumeWriter writer = new IPlantXMLVolumeWriter(outFile);
				
		writer.write(result);
		
		*/
	}
	
	private Treatment doTranslation(semanticMarkup.io.input.lib.xml.Treatment xmlTreatment) {
		semanticMarkup.io.input.lib.iplant.Treatment result = new semanticMarkup.io.input.lib.iplant.Treatment();
		
		result.setTaxonIdentification(new semanticMarkup.io.input.lib.iplant.TaxonIdentification());
		result.setMeta(new Meta());
		result.getMeta().setSource("FNA " + xmlTreatment.getNumber());
		
        List<Object> taxonIdentificationOrEtymologyOrOtherInfo = xmlTreatment.getTaxonIdentificationOrEtymologyOrOtherInfo();
        List<Object> referencesOrKey = xmlTreatment.getReferencesOrKey();
        
        for(Object item : taxonIdentificationOrEtymologyOrOtherInfo) {
        	if(item instanceof JAXBElement) {
        		JAXBElement<String> element = (JAXBElement<String>) item;
        		if(element.getValue() != null) {
	        		if(element.getName().toString().equals("description"))
						result.setDescription(element.getValue());
	        		//else
	        		//	result.getDiscussion().add(element.getName() + ": " + element.getValue());
        		}
        	}
        	if(item instanceof TaxonIdentification) {
        		TaxonIdentification taxonIdentification = (TaxonIdentification)item;
        		List<Object> taxonIdentificationContent = taxonIdentification.getContent();
        		
        		for(Object content : taxonIdentificationContent) {
        			if(content instanceof JAXBElement) {
        				JAXBElement<String> element = (JAXBElement<String>) content;
        				if(element.getValue() != null) {
	        				if(element.getName().toString().equals("genus_name"))
	        					result.getTaxonIdentification().setTaxonName(element.getValue());
	        				else 
	        					result.getTaxonIdentification().getOtherInfo().add(element.getName() + ": " + element.getValue());
        				}
        			}
        			if(content instanceof PlaceOfPublication) {
        				PlaceOfPublication placeOfPublication = (PlaceOfPublication)content;
        				
        				if(placeOfPublication.getPlaceInPublication() != null)
        					result.getTaxonIdentification().getOtherInfo().add(placeOfPublication.getPlaceInPublication());
        				if(placeOfPublication.getPublicationTitle() != null)
        					result.getTaxonIdentification().getOtherInfo().add(placeOfPublication.getPublicationTitle());
        	
        				for(String otherInfo : placeOfPublication.getOtherInfo()) 
        					if(otherInfo != null)
        						result.getTaxonIdentification().getOtherInfo().add(otherInfo);
        			}
        		}
        		
        		if(taxonIdentification.getStatus() != null)
        			result.getTaxonIdentification().getOtherInfo().add("Status: " + taxonIdentification.getStatus());
        	}
        }
        
        for(Object item : referencesOrKey) {
        	if(item instanceof Key) {
        		Key key = (Key)item;
        		
        		/*if(key.getKeyHeading() != null)
        			result.getDiscussion().add("key_heading: " + key.getKeyHeading());
        		for(String author : key.getKeyAuthor())
        			result.getDiscussion().add("author: " + author);
        		*/
        		
        		List<Object> keyDiscussionOrKeyHeadOrKeyStatements = key.getKeyDiscussionOrKeyHeadOrKeyStatement();
        		for(Object keyDiscussionOrKeyHeadOrKeyStatement : keyDiscussionOrKeyHeadOrKeyStatements) {
        			if(keyDiscussionOrKeyHeadOrKeyStatement instanceof JAXBElement) {
        				JAXBElement<String> element = (JAXBElement<String>)keyDiscussionOrKeyHeadOrKeyStatement;
        				if(element.getValue() != null) {
        					//result.getDiscussion().add(element.getName().toString() + ": " + element.getValue());
        				}
        			}
        			if(keyDiscussionOrKeyHeadOrKeyStatement instanceof KeyStatement) {
        				KeyStatement keyStatement = (KeyStatement)keyDiscussionOrKeyHeadOrKeyStatement;
        				//if(keyStatement.getDetermination() != null)
        					//result.getDiscussion().add("determination: " + keyStatement.getDetermination());
        				//if(keyStatement.getNextStatementId() != null)
        					//result.getDiscussion().add("next_statement_id: " + keyStatement.getNextStatementId());
        				//if(keyStatement.getStatement() != null)
        					//result.getDiscussion().add("statement: " + keyStatement.getStatement());
        				//if(keyStatement.getStatementId() != null)
        					//result.getDiscussion().add("statement_id: " + keyStatement.getStatementId());
        			}
        		}
        	}
        	if(item instanceof References) {
        		References references = (References)item;
        		if(references.getHeading() != null)
        			//result.getDiscussion().add("heading: " + references.getHeading());
        		
        		for(String reference : references.getReference()) {
    				//result.getDiscussion().add("reference: " + reference);
        		}
        		for(String referenceDiscussion : references.getReferenceDiscussion()) {
        			//result.getDiscussion().add("reference_discussion: " + referenceDiscussion);
        		}
        	}
        }
		
		
		
		return result;
	}

	public static void main(String[] args) throws Exception {
		String inFile = "C:\\in3\\";
		String outFile = "C:\\out3\\";
		FNAXMLToIPlantXMLTransformer translator = new FNAXMLToIPlantXMLTransformer(inFile, outFile);
		translator.translate();
	}
}
