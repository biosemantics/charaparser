package semanticMarkup.io.input.lib.xmlOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.io.output.lib.xml.Character;
import semanticMarkup.io.output.lib.xml.Description;
import semanticMarkup.io.output.lib.xml.DescriptionStatement;
import semanticMarkup.io.output.lib.xml.Key;
import semanticMarkup.io.output.lib.xml.KeyStatement;
import semanticMarkup.io.output.lib.xml.PlaceOfPublication;
import semanticMarkup.io.output.lib.xml.References;
import semanticMarkup.io.output.lib.xml.Relation;
import semanticMarkup.io.output.lib.xml.Structure;
import semanticMarkup.io.output.lib.xml.TaxonIdentification;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * OutputVolumeReader reads a list of treatments from the output file of XML2VolumeReader
 * @author rodenhausen
 */
public class OutputVolumeReader extends AbstractFileVolumeReader {
	
	/**
	 * @param filepath
	 */
	@Inject
	public OutputVolumeReader(@Named("OutputVolumeReader_SourceDirectory")String sourceDirectory) {
		super(sourceDirectory);
	}

	@Override
	public List<Treatment> read() throws Exception {        
		List<Treatment> result = new ArrayList<Treatment>();
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.xml");
        Unmarshaller u = jc.createUnmarshaller();
        
        File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			File file = files[i];
			if(file.getName().endsWith(".xml")) {
		        semanticMarkup.io.output.lib.xml.Treatment xmlTreatment = (semanticMarkup.io.output.lib.xml.Treatment)u.unmarshal(file);
		        Treatment treatment = transformTreatment(xmlTreatment);
		        treatment.setName(file.getName().split("\\.(?=[^\\.]+$)")[0]);
		        result.add(treatment);
			}
		}
        return result;
	}

	private Treatment transformTreatment(semanticMarkup.io.output.lib.xml.Treatment xmlTreatment) {
        Treatment treatment = new Treatment();
        treatment.addTreatmentElement(new ValueTreatmentElement("number", xmlTreatment.getNumber()));

        transformTaxonIdentificationOrEtymologyOrOtherInfo(xmlTreatment.getTaxonIdentificationOrEtymologyOrOtherInfo(), treatment);
        transformReferencesOrKey(xmlTreatment.getReferencesOrKey(), treatment);

        return treatment;
	}

	private void transformTaxonIdentificationOrEtymologyOrOtherInfo(List<Object> taxonIdentificationOrEtymologyOrOtherInfo,
			Treatment treatment) {
		for(Object item : taxonIdentificationOrEtymologyOrOtherInfo) {
        	if(item instanceof JAXBElement) {
        		JAXBElement<String> element = (JAXBElement<String>) item;
        		treatment.addTreatmentElement(new ValueTreatmentElement(element.getName().toString(), element.getValue()));
        	}
			if(item instanceof Description) {
        		Description description = (Description)item;
        		transformDescription(description, treatment);
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
	}

	private void transformReferencesOrKey(List<Object> referencesOrKey,	Treatment treatment) {
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
	}

	private void transformDescription(Description description, Treatment treatment) {
		ContainerTreatmentElement descriptionElement = new ContainerTreatmentElement("description");
		treatment.addTreatmentElement(descriptionElement);
		
		for(DescriptionStatement descriptionStatement : description.getDescriptionStatement()) {
			DescriptionTreatmentElement statementElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STATEMENT);
			statementElement.setAttribute("text", descriptionStatement.getText());
			statementElement.setAttribute("id", descriptionStatement.getId());
			statementElement.setAttribute("notes", descriptionStatement.getNotes());
			statementElement.setAttribute("provenance", descriptionStatement.getProvenance());	
			
			for(Object relationOrStructure : descriptionStatement.getRelationOrStructure()) {
				if(relationOrStructure instanceof Structure) {
					Structure structure = (Structure)relationOrStructure;
					DescriptionTreatmentElement structureElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STRUCTURE);
					statementElement.addTreatmentElement(structureElement);
					
					structureElement.setAttribute("alter_name", structure.getAlterName());
					structureElement.setAttribute("constraint", structure.getConstraint());
					structureElement.setAttribute("id", structure.getId());
					structureElement.setAttribute("name", structure.getName());
					structureElement.setAttribute("constraintid", structure.getConstraintid());
					structureElement.setAttribute("geographical_constraint", structure.getGeographicalConstraint());
					structureElement.setAttribute("in_bracket", String.valueOf(structure.isInBracket()));
					structureElement.setAttribute("in_brackets", String.valueOf(structure.getAlterName()));
					structureElement.setAttribute("notes", structure.getNotes());
					structureElement.setAttribute("ontologyid", structure.getOntologyid());
					structureElement.setAttribute("parallelism_constraint", structure.getParallelismConstraint());
					structureElement.setAttribute("provenance", structure.getProvenance());
					structureElement.setAttribute("taxon_constraint", structure.getTaxonConstraint());
	
					for(Character character : structure.getCharacter()) {
						DescriptionTreatmentElement characterElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.CHARACTER);
						structureElement.addTreatmentElement(characterElement);
						
						characterElement.setAttribute("char_type", character.getCharType());
						characterElement.setAttribute("constraint", character.getConstraint());
						characterElement.setAttribute("constraintid", character.getConstraintid());
						characterElement.setAttribute("from", character.getFrom());
						characterElement.setAttribute("from_inclusive", String.valueOf(character.isFromInclusive()));
						characterElement.setAttribute("from_unit", character.getFromUnit());
						characterElement.setAttribute("geographical_constraint", character.getGeographicalConstraint());
						characterElement.setAttribute("in_brackets", String.valueOf(character.isInBrackets()));
						characterElement.setAttribute("modifier", character.getModifier());
						characterElement.setAttribute("name", character.getName());
						characterElement.setAttribute("notes", character.getNotes());
						characterElement.setAttribute("ontologyid", character.getOntologyid());
						characterElement.setAttribute("organ_constraint", character.getOrganConstraint());
						characterElement.setAttribute("other_constraint", character.getOtherConstraint());
						characterElement.setAttribute("parallelism_constraint", character.getParallelismConstraint());
						characterElement.setAttribute("provenance", character.getProvenance());
						characterElement.setAttribute("taxon_constraint", character.getTaxonConstraint());
						characterElement.setAttribute("to", character.getTo());
						characterElement.setAttribute("to_inclusive", String.valueOf(character.isToInclusive()));
						characterElement.setAttribute("to_unit", character.getToUnit());
						characterElement.setAttribute("type", character.getType());
						characterElement.setAttribute("unit", character.getUnit());
						characterElement.setAttribute("upper_restricted", String.valueOf(character.isUpperRestricted()));
						characterElement.setAttribute("value", character.getValue());
					}
				}
				
				if(relationOrStructure instanceof Relation) {
					Relation relation = (Relation)relationOrStructure;
					DescriptionTreatmentElement relationElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.RELATION);
					statementElement.addTreatmentElement(relationElement);
					relationElement.setAttribute("alter_name", relation.getAlterName());
					relationElement.setAttribute("from", relation.getFrom());
					relationElement.setAttribute("geographical_constraint", relation.getGeographicalConstraint());
					relationElement.setAttribute("id", relation.getId());
					relationElement.setAttribute("in_brackets", String.valueOf(relation.isInBrackets()));
					relationElement.setAttribute("modifier", relation.getModifier());
					relationElement.setAttribute("name", relation.getName());
					relationElement.setAttribute("negation", String.valueOf(relation.isNegation()));
					relationElement.setAttribute("notes", relation.getNotes());
					relationElement.setAttribute("ontologyid", relation.getOntologyid());
					relationElement.setAttribute("organ_constraint", relation.getOrganConstraint());
					relationElement.setAttribute("parallelism_constraint", relation.getParallelismConstraint());
					relationElement.setAttribute("provenance", relation.getProvenance());
					relationElement.setAttribute("taxon_constraint", relation.getTaxonConstraint());
					relationElement.setAttribute("to", relation.getTo());
				}
			}
		}
	}
}
