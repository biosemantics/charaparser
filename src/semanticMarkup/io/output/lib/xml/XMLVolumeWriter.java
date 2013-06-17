package semanticMarkup.io.output.lib.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * XMLVolumeWriter is not usable at this time
 * TODO: Work on Mapping TreatmentElement classes directly to JAXB instead of using the intermediate data container classes (this is done with
 * XML2VolumeWriter)
 * @author rodenhausen
 */
public class XMLVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public XMLVolumeWriter(@Named("Run_OutDirectory") String outDirectory) {
		super(outDirectory);
	}
	
	@Override
	public void write(List<Treatment> treatments) throws Exception {
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.xml");
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		for(Treatment treatment : treatments) {
			new File(outDirectory + File.separator + treatment.getName() + ".xml").getParentFile().mkdirs();
			FileOutputStream outputStream = new FileOutputStream(new File(outDirectory + File.separator + treatment.getName() + ".xml"));
			semanticMarkup.io.output.lib.xml.Treatment xmlTreatment = transformTreatment(treatment);
			m.marshal(xmlTreatment, outputStream);
		}
	}

	private semanticMarkup.io.output.lib.xml.Treatment transformTreatment(Treatment treatment) {
		semanticMarkup.io.output.lib.xml.Treatment xmlTreatment = new semanticMarkup.io.output.lib.xml.Treatment();
		xmlTreatment.setNumber(treatment.getName());
			
		List<Object> xmlTreatmentContent = xmlTreatment.getTaxonIdentificationOrEtymologyOrOtherInfo();
			
		createDescription(xmlTreatmentContent, treatment);
		createOtherXmlTreatmentContent(xmlTreatmentContent, treatment);
		createReferencesOrKey(xmlTreatment.getReferencesOrKey(), treatment);

		return xmlTreatment;
	}

	private void createReferencesOrKey(List<Object> referencesOrKey, Treatment treatment) {
		for(ContainerTreatmentElement containerTreatmentElement : treatment.getContainerTreatmentElements("key")) {
			Key key = new Key();
			
			for(ValueTreatmentElement valueTreatmentElement : containerTreatmentElement.getValueTreatmentElements()) {
				switch(valueTreatmentElement.getName()) {
				case "key_heading":
					key.setKeyHeading(valueTreatmentElement.getValue());
					break;
				case "author:":
					key.getKeyAuthor().add(valueTreatmentElement.getValue());
					break;
				default:
					key.getKeyDiscussionOrKeyHeadOrKeyStatement().add(
							new JAXBElement<String>(new QName("", valueTreatmentElement.getName()), String.class, valueTreatmentElement.getValue()));
				}
			}
			
			for(ContainerTreatmentElement keyStatementcontainerTreatmentElement : containerTreatmentElement.getContainerTreatmentElements("key_statement")) {
				KeyStatement keyStatement = new KeyStatement();
				keyStatement.setDetermination(keyStatementcontainerTreatmentElement.getValueTreatmentElement("determination").getValue());
				keyStatement.setNextStatementId(keyStatementcontainerTreatmentElement.getValueTreatmentElement("next_statement_id").getValue());
				keyStatement.setStatement(keyStatementcontainerTreatmentElement.getValueTreatmentElement("statement").getValue());
				keyStatement.setStatementId(keyStatementcontainerTreatmentElement.getValueTreatmentElement("statement_id").getValue());
			}
			
			referencesOrKey.add(key);
		}
		
		for(ContainerTreatmentElement containerTreatmentElement : treatment.getContainerTreatmentElements("references")) {
			References references = new References();
			references.setHeading(containerTreatmentElement.getAttribute("heading"));
			
			for(ValueTreatmentElement valueTreatmentElement : containerTreatmentElement.getValueTreatmentElements("reference")) {
				references.getReference().add(valueTreatmentElement.getValue());
			}
			for(ValueTreatmentElement valueTreatmentElement : containerTreatmentElement.getValueTreatmentElements("reference_discussion")) {
				references.getReferenceDiscussion().add(valueTreatmentElement.getValue());
			}
			
			referencesOrKey.add(references);
		}
	}

	private void createOtherXmlTreatmentContent(List<Object> xmlTreatmentContent, Treatment treatment) {
		for(ContainerTreatmentElement containerTreatmentElement : treatment.getContainerTreatmentElements("TaxonIdentification")) {
			TaxonIdentification taxonIdentification = new TaxonIdentification();
			taxonIdentification.setStatus(containerTreatmentElement.getAttribute("Status"));
			List<Object> taxonIdentificationContent = taxonIdentification.getContent();
			for(ValueTreatmentElement valueTreatmentElement : containerTreatmentElement.getValueTreatmentElements()) {
				taxonIdentificationContent.add(new JAXBElement<String>(new QName("", valueTreatmentElement.getName()), String.class, valueTreatmentElement.getValue()));
			}
			for(ContainerTreatmentElement placeOfPublicationContainerTreatmentElement : containerTreatmentElement.getContainerTreatmentElements("place_of_publication")) {
				PlaceOfPublication placeOfPublication = new PlaceOfPublication();
				placeOfPublication.setPlaceInPublication(placeOfPublicationContainerTreatmentElement.getValueTreatmentElement("place_in_publication").getValue());
				placeOfPublication.setPublicationTitle(	placeOfPublicationContainerTreatmentElement.getValueTreatmentElement("publication_title").getValue());
				for(ValueTreatmentElement valueTreatmentElement : placeOfPublicationContainerTreatmentElement.getValueTreatmentElements("other_info")) {
					placeOfPublication.getOtherInfo().add(valueTreatmentElement.getValue());
				}
				taxonIdentificationContent.add(placeOfPublication);
			}
			xmlTreatmentContent.add(taxonIdentification);
		}
		
		for(ValueTreatmentElement valueTreatmentElement : treatment.getValueTreatmentElements()) {
			xmlTreatmentContent.add(new JAXBElement<String>(new QName("", valueTreatmentElement.getName()), String.class, valueTreatmentElement.getValue()));
		}
	}

	private void createDescription(List<Object> xmlTreatmentContent, Treatment treatment) {
		ContainerTreatmentElement description = treatment.getContainerTreatmentElement("description");
		
		Description newDescription = new Description();
		List<DescriptionStatement> newStatements = newDescription.getDescriptionStatement();
		for(ContainerTreatmentElement statement : description.getContainerTreatmentElements(DescriptionTreatmentElementType.STATEMENT.toString())) {
			DescriptionTreatmentElement statementElement = (DescriptionTreatmentElement)statement;
			DescriptionStatement newStatement = new DescriptionStatement();
			newStatement.setText(statementElement.getAttribute("text"));
			newStatement.setId(statementElement.getAttribute("id"));
			newStatement.setNotes(statementElement.getAttribute("notes"));
			newStatement.setProvenance(statementElement.getAttribute("provenenance"));
			
			List<Object> newRelationOrStructure = newStatement.getRelationOrStructure();
			for(ContainerTreatmentElement structure : statementElement.getContainerTreatmentElements(DescriptionTreatmentElementType.STRUCTURE.toString())) {
				DescriptionTreatmentElement structureElement = (DescriptionTreatmentElement)structure;
				Structure newStructure = new Structure();
				newStructure.setConstraint(structureElement.getAttribute("constraint"));
				newStructure.setId(structureElement.getAttribute("id"));
				newStructure.setName(structureElement.getAttribute("name"));
				newStructure.setAlterName(structureElement.getAttribute("alter_name"));
				newStructure.setConstraintid(structureElement.getAttribute("constraintid"));
				newStructure.setGeographicalConstraint(structureElement.getAttribute("geographical_constraint"));
				newStructure.setInBracket(getBooleanFromString(structureElement.getAttribute("in_bracket")));
				newStructure.setInBrackets(getBooleanFromString(structureElement.getAttribute("in_brackets")));
				newStructure.setNotes(structureElement.getAttribute("notes"));
				newStructure.setOntologyid(structureElement.getAttribute("ontologyid"));
				newStructure.setParallelismConstraint(structureElement.getAttribute("parallelism_constraint"));
				newStructure.setProvenance(structureElement.getAttribute("provenance"));
				newStructure.setTaxonConstraint(structureElement.getAttribute("taxon_constraint"));
				
				List<Character> newCharacterList = newStructure.getCharacter();
				for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
					DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
					//if(character.getCh
					
					Character newCharacter = new Character();
					newCharacter.setCharType(characterElement.getAttribute("char_type"));
					newCharacter.setConstraint(characterElement.getAttribute("constraint"));
					newCharacter.setConstraintid(characterElement.getAttribute("constraintid"));
					newCharacter.setFrom(characterElement.getAttribute("from"));
					newCharacter.setFromInclusive(getBooleanFromString(characterElement.getAttribute("from_inclusive")));
					newCharacter.setFromUnit(characterElement.getAttribute("from_unit"));
					newCharacter.setGeographicalConstraint(characterElement.getAttribute("geographical_constraint"));
					newCharacter.setInBrackets(getBooleanFromString(characterElement.getAttribute("in_brackets")));
					newCharacter.setModifier(characterElement.getAttribute("modifier"));
					newCharacter.setName(characterElement.getAttribute("name"));
					newCharacter.setNotes(characterElement.getAttribute("notes"));
					newCharacter.setOntologyid(characterElement.getAttribute("ontologyid"));
					newCharacter.setOrganConstraint(characterElement.getAttribute("organ_constraint"));
					newCharacter.setOtherConstraint(characterElement.getAttribute("other_constraint"));
					newCharacter.setParallelismConstraint(characterElement.getAttribute("parallelism_constraint"));
					newCharacter.setProvenance(characterElement.getAttribute("provenance"));
					newCharacter.setTaxonConstraint(characterElement.getAttribute("taxon_constraint"));
					newCharacter.setTo(characterElement.getAttribute("to"));
					newCharacter.setToInclusive(getBooleanFromString(characterElement.getAttribute("to_inclusive")));
					newCharacter.setToUnit(characterElement.getAttribute("to_unit"));
					newCharacter.setType(characterElement.getAttribute("type"));
					newCharacter.setUnit(characterElement.getAttribute("unit"));
					newCharacter.setUpperRestricted(getBooleanFromString(characterElement.getAttribute("upper_restricted")));
					newCharacter.setValue(characterElement.getAttribute("value"));    
					newCharacterList.add(newCharacter);
				}
				newRelationOrStructure.add(newStructure);
			}
			
			for(TreatmentElement relation : statementElement.getTreatmentElements(DescriptionTreatmentElementType.RELATION.toString())) {
				DescriptionTreatmentElement relationElement = (DescriptionTreatmentElement)relation;
				
				Relation newRelation = new Relation();
				newRelation.setAlterName(relationElement.getAttribute("alter_name"));
				newRelation.setFrom(relationElement.getAttribute("from"));
				newRelation.setGeographicalConstraint(relationElement.getAttribute("geographical_constraint"));
				newRelation.setId(relationElement.getAttribute("id"));
				newRelation.setInBrackets(getBooleanFromString(relationElement.getAttribute("in_brackets")));
				newRelation.setModifier(relationElement.getAttribute("modifier"));
				newRelation.setName(relationElement.getAttribute("name"));
				newRelation.setNegation(getBooleanFromString(relationElement.getAttribute("negation")));
				newRelation.setNotes(relationElement.getAttribute("notes"));
				newRelation.setOntologyid(relationElement.getAttribute("ontologyid"));
				newRelation.setOrganConstraint(relationElement.getAttribute("organ_constraint"));
				newRelation.setParallelismConstraint(relationElement.getAttribute("parallelism_constraint"));
				newRelation.setProvenance(relationElement.getAttribute("provenance"));
				newRelation.setTaxonConstraint(relationElement.getAttribute("taxon_constraint"));
				newRelation.setTo(relationElement.getAttribute("to"));
				
				newRelationOrStructure.add(newRelation);
			}
			newStatements.add(newStatement);
		}
		xmlTreatmentContent.add(newDescription);
	}
	
	private Boolean getBooleanFromString(String string) {
		Boolean result = null;
		if(string == null)
			return result;
		if(string.equals("true") || string.equals("false"))
			result = Boolean.valueOf(string);
		return result;
	}
}
