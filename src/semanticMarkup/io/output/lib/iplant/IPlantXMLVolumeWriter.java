package semanticMarkup.io.output.lib.iplant;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import semanticMarkup.io.output.AbstractFileVolumeWriter;
import semanticMarkup.model.ContainerTreatmentElement;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.TreatmentElement;
import semanticMarkup.model.ValueTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElementType;
import semanticMarkup.model.description.attributes.CharacterAttribute;
import semanticMarkup.model.description.attributes.RelationAttribute;
import semanticMarkup.model.description.attributes.StatementAttribute;
import semanticMarkup.model.description.attributes.StructureAttribute;

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
			newStatement.setText(statementElement.getAttribute(StatementAttribute.text));
			newStatement.setId(statementElement.getAttribute(StatementAttribute.id));
			newStatement.setNotes(statementElement.getAttribute(StatementAttribute.notes));
			newStatement.setProvenance(statementElement.getAttribute(StatementAttribute.provenance));
			
			List<Object> newRelationOrStructure = newStatement.getRelationOrStructure();
			for(ContainerTreatmentElement structure : statementElement.getContainerTreatmentElements(DescriptionTreatmentElementType.STRUCTURE.toString())) {
				DescriptionTreatmentElement structureElement = (DescriptionTreatmentElement)structure;
				Structure newStructure = new Structure();
				newStructure.setConstraint(structureElement.getAttribute(StructureAttribute.constraint));
				newStructure.setId(structureElement.getAttribute(StructureAttribute.id));
				newStructure.setName(structureElement.getAttribute(StructureAttribute.name));
				newStructure.setAlterName(structureElement.getAttribute(StructureAttribute.alter_name));
				newStructure.setConstraintid(structureElement.getAttribute(StructureAttribute.constraintid));
				newStructure.setGeographicalConstraint(structureElement.getAttribute(StructureAttribute.geographical_constraint));
				newStructure.setInBracket(getBooleanFromString(structureElement.getAttribute(StructureAttribute.in_bracket)));
				newStructure.setInBrackets(getBooleanFromString(structureElement.getAttribute(StructureAttribute.in_brackets)));
				newStructure.setNotes(structureElement.getAttribute(StructureAttribute.notes));
				newStructure.setOntologyid(structureElement.getAttribute(StructureAttribute.ontologyid));
				newStructure.setParallelismConstraint(structureElement.getAttribute(StructureAttribute.parallelism_constraint));
				newStructure.setProvenance(structureElement.getAttribute(StructureAttribute.provenance));
				newStructure.setTaxonConstraint(structureElement.getAttribute(StructureAttribute.taxon_constraint));
				
				List<Character> newCharacterList = newStructure.getCharacter();
				for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
					DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
					Character newCharacter = new Character();
					newCharacter.setCharType(characterElement.getAttribute(CharacterAttribute.char_type));
					newCharacter.setConstraint(characterElement.getAttribute(CharacterAttribute.constraint));
					newCharacter.setConstraintid(characterElement.getAttribute(CharacterAttribute.constraintid));
					newCharacter.setFrom(characterElement.getAttribute(CharacterAttribute.from));
					newCharacter.setFromInclusive(getBooleanFromString(characterElement.getAttribute(CharacterAttribute.from_inclusive)));
					newCharacter.setFromUnit(characterElement.getAttribute(CharacterAttribute.from_unit));
					newCharacter.setGeographicalConstraint(characterElement.getAttribute(CharacterAttribute.geographical_constraint));
					newCharacter.setInBrackets(getBooleanFromString(characterElement.getAttribute(CharacterAttribute.in_brackets)));
					newCharacter.setModifier(characterElement.getAttribute(CharacterAttribute.modifier));
					newCharacter.setName(characterElement.getAttribute(CharacterAttribute.name));
					newCharacter.setNotes(characterElement.getAttribute(CharacterAttribute.notes));
					newCharacter.setOntologyid(characterElement.getAttribute(CharacterAttribute.ontologyid));
					newCharacter.setOrganConstraint(characterElement.getAttribute(CharacterAttribute.organ_constraint));
					newCharacter.setOtherConstraint(characterElement.getAttribute(CharacterAttribute.other_constraint));
					newCharacter.setParallelismConstraint(characterElement.getAttribute(CharacterAttribute.parallelism_constraint));
					newCharacter.setProvenance(characterElement.getAttribute(CharacterAttribute.provenance));
					newCharacter.setTaxonConstraint(characterElement.getAttribute(CharacterAttribute.taxon_constraint));
					newCharacter.setTo(characterElement.getAttribute(CharacterAttribute.to));
					newCharacter.setToInclusive(getBooleanFromString(characterElement.getAttribute(CharacterAttribute.to_inclusive)));
					newCharacter.setToUnit(characterElement.getAttribute(CharacterAttribute.to_unit));
					newCharacter.setType(characterElement.getAttribute(CharacterAttribute.type));
					newCharacter.setUnit(characterElement.getAttribute(CharacterAttribute.unit));
					newCharacter.setUpperRestricted(getBooleanFromString(characterElement.getAttribute(CharacterAttribute.upper_restricted)));
					newCharacter.setValue(characterElement.getAttribute(CharacterAttribute.value));    
					newCharacterList.add(newCharacter);
				}
				newRelationOrStructure.add(newStructure);
			}
			
			for(TreatmentElement relation : statementElement.getTreatmentElements(DescriptionTreatmentElementType.RELATION.toString())) {
				DescriptionTreatmentElement relationElement = (DescriptionTreatmentElement)relation;
				
				Relation newRelation = new Relation();
				newRelation.setAlterName(relationElement.getAttribute(RelationAttribute.alter_name));
				newRelation.setFrom(relationElement.getAttribute(RelationAttribute.from));
				newRelation.setGeographicalConstraint(relationElement.getAttribute(RelationAttribute.geographical_constraint));
				newRelation.setId(relationElement.getAttribute(RelationAttribute.id));
				newRelation.setInBrackets(getBooleanFromString(relationElement.getAttribute(RelationAttribute.in_brackets)));
				newRelation.setModifier(relationElement.getAttribute(RelationAttribute.modifier));
				newRelation.setName(relationElement.getAttribute(RelationAttribute.name));
				newRelation.setNegation(getBooleanFromString(relationElement.getAttribute(RelationAttribute.negation)));
				newRelation.setNotes(relationElement.getAttribute(RelationAttribute.notes));
				newRelation.setOntologyid(relationElement.getAttribute(RelationAttribute.ontologyid));
				newRelation.setOrganConstraint(relationElement.getAttribute(RelationAttribute.organ_constraint));
				newRelation.setParallelismConstraint(relationElement.getAttribute(RelationAttribute.parallelism_constraint));
				newRelation.setProvenance(relationElement.getAttribute(RelationAttribute.provenance));
				newRelation.setTaxonConstraint(relationElement.getAttribute(RelationAttribute.taxon_constraint));
				newRelation.setTo(relationElement.getAttribute(RelationAttribute.to));
				
				newRelationOrStructure.add(newRelation);
			}
			newStatements.add(newStatement);
		}
		xmlTreatment.setDescription(newDescription);
	}
	
	private Boolean getBooleanFromString(String string) {
		Boolean result = null;
		if(string != null && (string.equals("true") || string.equals("false")))
			result = Boolean.valueOf(string);
		return result;
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
