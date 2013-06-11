package semanticMarkup.io.output.lib.iplant;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.io.output.AbstractFileVolumeWriter;
import semanticMarkup.io.output.lib.iplant.Character;
import semanticMarkup.io.output.lib.iplant.Description;
import semanticMarkup.io.output.lib.iplant.DescriptionStatement;
import semanticMarkup.io.output.lib.iplant.Relation;
import semanticMarkup.io.output.lib.iplant.Structure;
import semanticMarkup.io.output.lib.iplant.TaxonIdentification;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class IPlantXMLVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public IPlantXMLVolumeWriter(@Named("Run_OutDirectory") String outDirectory) {
		super(outDirectory);
	}
	
	@Override
	public void write(List<Treatment> treatments) throws Exception {
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.iplant");
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		for(Treatment treatment : treatments) {
			new File(outDirectory + File.separator + treatment.getName() + ".xml").getParentFile().mkdirs();
			FileOutputStream outputStream = new FileOutputStream(new File(outDirectory + File.separator + treatment.getName() + ".xml"));
			semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment = transformTreatment(treatment);
			m.marshal(xmlTreatment, outputStream);
		}
	}

	private semanticMarkup.io.output.lib.iplant.Treatment transformTreatment(Treatment treatment) {
		semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment = new semanticMarkup.io.output.lib.iplant.Treatment();		
		createMeta(treatment, xmlTreatment);
		createTaxonIdentification(treatment, xmlTreatment);
		createDescription(treatment, xmlTreatment);
		createDiscussion(treatment, xmlTreatment);
		return xmlTreatment;
	}

	private void createDescription(Treatment treatment, semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment) {
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
				newStructure.setInBracket(Boolean.valueOf(structureElement.getAttribute("in_bracket")));
				newStructure.setInBrackets(Boolean.valueOf(structureElement.getAttribute("in_brackets")));
				newStructure.setNotes(structureElement.getAttribute("notes"));
				newStructure.setOntologyid(structureElement.getAttribute("ontologyid"));
				newStructure.setParallelismConstraint(structureElement.getAttribute("parallelism_constraint"));
				newStructure.setProvenance(structureElement.getAttribute("provenance"));
				newStructure.setTaxonConstraint(structureElement.getAttribute("taxon_constraint"));
				
				List<Character> newCharacterList = newStructure.getCharacter();
				for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
					DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
					Character newCharacter = new Character();
					newCharacter.setCharType(characterElement.getAttribute("char_type"));
					newCharacter.setConstraint(characterElement.getAttribute("constraint"));
					newCharacter.setConstraintid(characterElement.getAttribute("constraintid"));
					newCharacter.setFrom(characterElement.getAttribute("from"));
					newCharacter.setFromInclusive(Boolean.valueOf(characterElement.getAttribute("from_inclusive")));
					newCharacter.setFromUnit(characterElement.getAttribute("from_unit"));
					newCharacter.setGeographicalConstraint(characterElement.getAttribute("geographical_constraint"));
					newCharacter.setInBrackets(Boolean.valueOf(characterElement.getAttribute("in_brackets")));
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
					newCharacter.setToInclusive(Boolean.valueOf(characterElement.getAttribute("to_inclusive")));
					newCharacter.setToUnit(characterElement.getAttribute("to_unit"));
					newCharacter.setType(characterElement.getAttribute("type"));
					newCharacter.setUnit(characterElement.getAttribute("unit"));
					newCharacter.setUpperRestricted(Boolean.valueOf(characterElement.getAttribute("upper_restricted")));
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
				newRelation.setInBrackets(Boolean.valueOf(relationElement.getAttribute("in_brackets")));
				newRelation.setModifier(relationElement.getAttribute("modifier"));
				newRelation.setName(relationElement.getAttribute("name"));
				newRelation.setNegation(Boolean.valueOf(relationElement.getAttribute("negation")));
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

	private void createDiscussion(Treatment treatment,
			semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment) {
		for(ValueTreatmentElement discussionElement : treatment.getValueTreatmentElements("discussion")) {
			xmlTreatment.getDiscussion().add(discussionElement.getValue());
		}
	}

	private void createTaxonIdentification(Treatment treatment, semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment) {
		ContainerTreatmentElement taxonIdentificationElement = treatment.getContainerTreatmentElement("TaxonIdentification");
		
		TaxonIdentification taxonIdentification = new TaxonIdentification();
		ValueTreatmentElement taxonNameElement = taxonIdentificationElement.getValueTreatmentElement("taxon_name");
		taxonIdentification.setTaxonName(taxonNameElement.getValue());
		for(ValueTreatmentElement otherInfoElement : taxonIdentificationElement.getValueTreatmentElements("other_info")) {
			taxonIdentification.getOtherInfo().add(otherInfoElement.getValue());
		}
		xmlTreatment.setTaxonIdentification(taxonIdentification);
		
	}

	private void createMeta(Treatment treatment, semanticMarkup.io.output.lib.iplant.Treatment xmlTreatment) {
		ContainerTreatmentElement metaElement = treatment.getContainerTreatmentElement("meta");
		
		Meta meta = new Meta();
		ValueTreatmentElement charaparserVersionElement = metaElement.getValueTreatmentElement("charaparser_version");
		ValueTreatmentElement glossaryNameElement = metaElement.getValueTreatmentElement("glossary_name");
		ValueTreatmentElement glossaryVersionElement = metaElement.getValueTreatmentElement("glossary_version");
		if(charaparserVersionElement != null)
			meta.setCharaparserVersion(charaparserVersionElement.getValue());
		if(glossaryNameElement != null)
			meta.setGlossaryName(glossaryNameElement.getValue());
		if(glossaryVersionElement != null)
			meta.setGlossaryVersion(glossaryVersionElement.getValue());
		ValueTreatmentElement sourceElement = metaElement.getValueTreatmentElement("source");
		meta.setSource(sourceElement.getValue());
		for(ValueTreatmentElement otherInfoElement : metaElement.getValueTreatmentElements("other_info")) {
			meta.getOtherInfo().add(otherInfoElement.getValue());
		}
		xmlTreatment.setMeta(meta);
	}
}
