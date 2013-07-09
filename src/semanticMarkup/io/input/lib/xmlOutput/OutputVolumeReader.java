package semanticMarkup.io.input.lib.xmlOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

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
import semanticMarkup.model.ContainerTreatmentElement;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.ValueTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElementType;
import semanticMarkup.model.description.attributes.CharacterAttribute;
import semanticMarkup.model.description.attributes.RelationAttribute;
import semanticMarkup.model.description.attributes.StatementAttribute;
import semanticMarkup.model.description.attributes.StructureAttribute;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * OutputVolumeReader reads a list of treatments from the output file of XML2VolumeReader
 * @author rodenhausen
 */
public class OutputVolumeReader extends AbstractFileVolumeReader {
	
	/**
	 * @param sourceDirectory
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
			statementElement.setAttribute(StatementAttribute.text, descriptionStatement.getText());
			statementElement.setAttribute(StatementAttribute.id, descriptionStatement.getId());
			statementElement.setAttribute(StatementAttribute.notes, descriptionStatement.getNotes());
			statementElement.setAttribute(StatementAttribute.provenance, descriptionStatement.getProvenance());	
			
			for(Object relationOrStructure : descriptionStatement.getRelationOrStructure()) {
				if(relationOrStructure instanceof Structure) {
					Structure structure = (Structure)relationOrStructure;
					DescriptionTreatmentElement structureElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STRUCTURE);
					statementElement.addTreatmentElement(structureElement);
					
					structureElement.setAttribute(StructureAttribute.alter_name, structure.getAlterName());
					structureElement.setAttribute(StructureAttribute.constraint, structure.getConstraint());
					structureElement.setAttribute(StructureAttribute.id, structure.getId());
					structureElement.setAttribute(StructureAttribute.name, structure.getName());
					structureElement.setAttribute(StructureAttribute.constraintid, structure.getConstraintid());
					structureElement.setAttribute(StructureAttribute.geographical_constraint, structure.getGeographicalConstraint());
					structureElement.setAttribute(StructureAttribute.in_bracket, String.valueOf(structure.isInBracket()));
					structureElement.setAttribute(StructureAttribute.in_brackets, String.valueOf(structure.getAlterName()));
					structureElement.setAttribute(StructureAttribute.notes, structure.getNotes());
					structureElement.setAttribute(StructureAttribute.ontologyid, structure.getOntologyid());
					structureElement.setAttribute(StructureAttribute.parallelism_constraint, structure.getParallelismConstraint());
					structureElement.setAttribute(StructureAttribute.provenance, structure.getProvenance());
					structureElement.setAttribute(StructureAttribute.taxon_constraint, structure.getTaxonConstraint());
	
					for(Character character : structure.getCharacter()) {
						DescriptionTreatmentElement characterElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.CHARACTER);
						structureElement.addTreatmentElement(characterElement);
						
						characterElement.setAttribute(CharacterAttribute.char_type, character.getCharType());
						characterElement.setAttribute(CharacterAttribute.constraint, character.getConstraint());
						characterElement.setAttribute(CharacterAttribute.constraintid, character.getConstraintid());
						characterElement.setAttribute(CharacterAttribute.from, character.getFrom());
						characterElement.setAttribute(CharacterAttribute.from_inclusive, String.valueOf(character.isFromInclusive()));
						characterElement.setAttribute(CharacterAttribute.from_unit, character.getFromUnit());
						characterElement.setAttribute(CharacterAttribute.geographical_constraint, character.getGeographicalConstraint());
						characterElement.setAttribute(CharacterAttribute.in_brackets, String.valueOf(character.isInBrackets()));
						characterElement.setAttribute(CharacterAttribute.modifier, character.getModifier());
						characterElement.setAttribute(CharacterAttribute.name, character.getName());
						characterElement.setAttribute(CharacterAttribute.notes, character.getNotes());
						characterElement.setAttribute(CharacterAttribute.ontologyid, character.getOntologyid());
						characterElement.setAttribute(CharacterAttribute.organ_constraint, character.getOrganConstraint());
						characterElement.setAttribute(CharacterAttribute.other_constraint, character.getOtherConstraint());
						characterElement.setAttribute(CharacterAttribute.parallelism_constraint, character.getParallelismConstraint());
						characterElement.setAttribute(CharacterAttribute.provenance, character.getProvenance());
						characterElement.setAttribute(CharacterAttribute.taxon_constraint, character.getTaxonConstraint());
						characterElement.setAttribute(CharacterAttribute.to, character.getTo());
						characterElement.setAttribute(CharacterAttribute.to_inclusive, String.valueOf(character.isToInclusive()));
						characterElement.setAttribute(CharacterAttribute.to_unit, character.getToUnit());
						characterElement.setAttribute(CharacterAttribute.type, character.getType());
						characterElement.setAttribute(CharacterAttribute.unit, character.getUnit());
						characterElement.setAttribute(CharacterAttribute.upper_restricted, String.valueOf(character.isUpperRestricted()));
						characterElement.setAttribute(CharacterAttribute.value, character.getValue());
					}
				}
				
				if(relationOrStructure instanceof Relation) {
					Relation relation = (Relation)relationOrStructure;
					DescriptionTreatmentElement relationElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.RELATION);
					statementElement.addTreatmentElement(relationElement);
					relationElement.setAttribute(RelationAttribute.alter_name, relation.getAlterName());
					relationElement.setAttribute(RelationAttribute.from, relation.getFrom());
					relationElement.setAttribute(RelationAttribute.geographical_constraint, relation.getGeographicalConstraint());
					relationElement.setAttribute(RelationAttribute.id, relation.getId());
					relationElement.setAttribute(RelationAttribute.in_brackets, String.valueOf(relation.isInBrackets()));
					relationElement.setAttribute(RelationAttribute.modifier, relation.getModifier());
					relationElement.setAttribute(RelationAttribute.name, relation.getName());
					relationElement.setAttribute(RelationAttribute.negation, String.valueOf(relation.isNegation()));
					relationElement.setAttribute(RelationAttribute.notes, relation.getNotes());
					relationElement.setAttribute(RelationAttribute.ontologyid, relation.getOntologyid());
					relationElement.setAttribute(RelationAttribute.organ_constraint, relation.getOrganConstraint());
					relationElement.setAttribute(RelationAttribute.parallelism_constraint, relation.getParallelismConstraint());
					relationElement.setAttribute(RelationAttribute.provenance, relation.getProvenance());
					relationElement.setAttribute(RelationAttribute.taxon_constraint, relation.getTaxonConstraint());
					relationElement.setAttribute(RelationAttribute.to, relation.getTo());
				}
			}
		}
	}
}
