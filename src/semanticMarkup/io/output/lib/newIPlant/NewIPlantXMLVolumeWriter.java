package semanticMarkup.io.output.lib.newIPlant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

public class NewIPlantXMLVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public NewIPlantXMLVolumeWriter(@Named("Run_OutDirectory") String outDirectory) {
		super(outDirectory);
	}
	
	@Override
	public void write(List<Treatment> treatments) throws Exception {		
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.newIPlant");
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		for(Treatment treatment : treatments) {
			new File(outDirectory + File.separator + treatment.getName() + ".xml").getParentFile().mkdirs();
			FileOutputStream outputStream = new FileOutputStream(new File(outDirectory + File.separator + treatment.getName() + ".xml"));
			Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
			semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment = transformTreatment(treatment);
			m.marshal(xmlTreatment, writer);
		}
	}

	private semanticMarkup.io.output.lib.newIPlant.Treatment transformTreatment(Treatment treatment) {
		semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment = new semanticMarkup.io.output.lib.newIPlant.Treatment();		
		createMeta(treatment, xmlTreatment);
		createTaxonIdentification(treatment, xmlTreatment);
		createDescription(treatment, xmlTreatment);
		createDiscussion(treatment, xmlTreatment);
		return xmlTreatment;
	}

	private void createMeta(Treatment treatment, semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment) {
		if(treatment.containsContainerTreatmentElement("meta")) {
			ContainerTreatmentElement metaElement = treatment.getContainerTreatmentElement("meta");
			
			Meta meta = new Meta();
			
			if(metaElement.containsValueTreatmentElement("source")) {
				ValueTreatmentElement sourceElement = metaElement.getValueTreatmentElement("source");
				meta.setSource(sourceElement.getValue());
			}
			
			if(metaElement.containsContainerTreatmentElement("processed_by")) {
				ContainerTreatmentElement processedByElement = metaElement.getContainerTreatmentElement("processed_by");
				ProcessedBy processedBy = new ProcessedBy();
				for(TreatmentElement treatmentElement : processedByElement.getTreatmentElements()) {
					if(treatmentElement.getName().equals("processor")) {
						ValueTreatmentElement processorElement = (ValueTreatmentElement)treatmentElement;
						Processor processor = new Processor();
						processor.setProcessType(processorElement.getAttribute("process_type"));
						processor.setValue(processorElement.getValue());
						processedBy.getProcessorOrCharaparser().add(processor);
					}
					if(treatmentElement.getName().equals("charaparser")) {
						ContainerTreatmentElement charaparserElement = (ContainerTreatmentElement)treatmentElement;
						Charaparser charaparser = new Charaparser();
						ValueTreatmentElement charaparserVersionElement = charaparserElement.getValueTreatmentElement("charaparser_version");
						if(charaparserVersionElement != null)
							charaparser.setCharaparserVersion(charaparserVersionElement.getValue());
						ValueTreatmentElement charaparserUserElement = charaparserElement.getValueTreatmentElement("charaparser_user");
						if(charaparserUserElement != null)
							charaparser.setCharaparserUser(charaparserUserElement.getValue());
						ValueTreatmentElement glossaryNameElement = charaparserElement.getValueTreatmentElement("glossary_name");
						if(glossaryNameElement != null)
							charaparser.setGlossaryName(glossaryNameElement.getValue());
						ValueTreatmentElement glossaryVersionElement = charaparserElement.getValueTreatmentElement("glossary_version");
						if(glossaryVersionElement != null)
							charaparser.setGlossaryVersion(glossaryVersionElement.getValue());
						processedBy.getProcessorOrCharaparser().add(charaparser);
					}
				}
				meta.setProcessedBy(processedBy);
			}
	
			for(ValueTreatmentElement otherInfoOnMetaElement : metaElement.getValueTreatmentElements("other_info_on_meta"))
				meta.getOtherInfoOnMeta().add(otherInfoOnMetaElement.getValue());
			
			xmlTreatment.setMeta(meta);
		}
	}
	
	private void createTaxonIdentification(Treatment treatment, semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment) {
		if(treatment.containsContainerTreatmentElement("taxon_identification")) {
			ContainerTreatmentElement taxonIdentificationElement = treatment.getContainerTreatmentElement("taxon_identification");
			
			TaxonIdentification taxonIdentification = new TaxonIdentification();
			List<ValueTreatmentElement> treatmentElements = taxonIdentificationElement.getValueTreatmentElements();
			for(TreatmentElement treatmentElement : treatmentElements) {
				if(treatmentElement instanceof ValueTreatmentElement) {
					ValueTreatmentElement valueTreatmentElement = (ValueTreatmentElement)treatmentElement;
					taxonIdentification.getContent().add(new JAXBElement<String>(new QName("", valueTreatmentElement.getName()), String.class, valueTreatmentElement.getValue()));
				}
				if(treatmentElement instanceof ContainerTreatmentElement) {
					ContainerTreatmentElement containerTreatmentElement = (ContainerTreatmentElement)treatmentElement;
					if(containerTreatmentElement.getName().equals("place_of_publication")) {
						PlaceOfPublication placeOfPublication = new PlaceOfPublication();
						if(containerTreatmentElement.containsValueTreatmentElement("publication_title"))
							placeOfPublication.setPublicationTitle(containerTreatmentElement.getValueTreatmentElement("publication_title").getValue());
						if(containerTreatmentElement.containsValueTreatmentElement("place_in_publication"))
							placeOfPublication.setPlaceInPublication(containerTreatmentElement.getValueTreatmentElement("place_in_publication").getValue());
						for(ValueTreatmentElement otherInfoOnPub : containerTreatmentElement.getValueTreatmentElements("other_info_on_pub"))
							placeOfPublication.getOtherInfoOnPub().add(otherInfoOnPub.getValue());
						taxonIdentification.getContent().add(placeOfPublication);
					}
				}
			}
			taxonIdentification.setStatus(taxonIdentificationElement.getAttribute("status"));
			xmlTreatment.setTaxonIdentification(taxonIdentification);
		}
	}
	
	private void createDescription(Treatment treatment, semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment) {
		if(treatment.containsContainerTreatmentElement("description")) {
			ContainerTreatmentElement description = treatment.getContainerTreatmentElement("description");
			
			Description newDescription = new Description();
			List<Statement> newStatements = newDescription.getStatement();
			for(ContainerTreatmentElement statement : description.getContainerTreatmentElements(DescriptionTreatmentElementType.STATEMENT.toString())) {
				DescriptionTreatmentElement statementElement = (DescriptionTreatmentElement)statement;
				Statement newStatement = new Statement();
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
					newStructure.setNameOriginal(structureElement.getAttribute("name_original"));
					
					List<Character> newCharacterList = newStructure.getCharacter();
					for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
						DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
						Character newCharacter = new Character();
						newCharacter.setCharType(characterElement.getAttribute("char_type"));
						newCharacter.setConstraint(characterElement.getAttribute("constraint"));
						newCharacter.setConstraintid(characterElement.getAttribute("constraintid"));
						newCharacter.setFrom(characterElement.getAttribute("from"));
						newCharacter.setFromInclusive(getBooleanFromString(characterElement.getAttribute("from_inclusive")));
						newCharacter.setFromUnit(characterElement.getAttribute("from_unit"));
						newCharacter.setGeographicalConstraint(characterElement.getAttribute("geographical_constraint"));
						newCharacter.setInBrackets(getBooleanFromString(characterElement.getAttribute("in_brakcets")));
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
			xmlTreatment.setDescription(newDescription);
		}
	}
	
	private Boolean getBooleanFromString(String string) {
		Boolean result = null;
		if(string != null && (string.equals("true") || string.equals("false")))
			result = Boolean.valueOf(string);
		return result;
	}
	
	
	private void createDiscussion(Treatment treatment, semanticMarkup.io.output.lib.newIPlant.Treatment xmlTreatment) {
		for(ValueTreatmentElement discussionElement : treatment.getValueTreatmentElements("discussion")) {
			xmlTreatment.getDiscussion().add(discussionElement.getValue());
		}
	}
}
