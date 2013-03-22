package semanticMarkup.io.input.lib.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.io.output.lib.xml2.Character;
import semanticMarkup.io.output.lib.xml2.Description;
import semanticMarkup.io.output.lib.xml2.Relation;
import semanticMarkup.io.output.lib.xml2.Statement;
import semanticMarkup.io.output.lib.xml2.Structure;
import semanticMarkup.io.output.lib.xml2.Treatments;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OutputVolumeReader extends AbstractFileVolumeReader {
	
	@Inject
	public OutputVolumeReader(@Named("OutputVolumeReader_Sourcefile")String filepath) {
		super(filepath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.xml2");
        Unmarshaller u = jc.createUnmarshaller();
        u.unmarshal(new File(filePath));
        Treatments treatments = (Treatments)u.unmarshal(new File(filePath + ".xml"));
        List<semanticMarkup.io.output.lib.xml2.Treatment> xmlTreatmentsList = treatments.getTreatment();
        List<Treatment> result = transformTreatments(xmlTreatmentsList);
        return result;
	}

	private List<Treatment> transformTreatments(List<semanticMarkup.io.output.lib.xml2.Treatment> xmlTreatmentsList) {
		ArrayList<Treatment> result = new ArrayList<Treatment>();
        
        for(semanticMarkup.io.output.lib.xml2.Treatment xmlTreatment : xmlTreatmentsList) {
        	Treatment treatment = new Treatment(xmlTreatment.getNumber());
        	
        	
        	Description xmlDescription = xmlTreatment.getDescription();
        	if(xmlDescription!=null) {
	        	ContainerTreatmentElement description = new ContainerTreatmentElement("description");
	        	treatment.addTreatmentElement(description);
	        	
	        	ArrayList<Statement> xmlStatements = xmlDescription.getStatement();
	        	for(Statement xmlStatement : xmlStatements) {
	        		DescriptionTreatmentElement statement = new DescriptionTreatmentElement(DescriptionType.STATEMENT);
	        		statement.setProperty("text", xmlStatement.getText());
	        		statement.setProperty("source", xmlStatement.getSource());
	        		description.addTreatmentElement(statement);
	        		
	        		ArrayList<Structure> xmlStructures = xmlStatement.getStructure();
	        		for(Structure xmlStructure : xmlStructures) {
	        			DescriptionTreatmentElement structure = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
	        			structure.setProperty("id", xmlStructure.getId());
	        			structure.setProperty("constraint", xmlStructure.getConstraint());
	        			structure.setProperty("name", xmlStructure.getName());
	        			statement.addTreatmentElement(structure);
	        			
	        			ArrayList<Character> xmlCharacters = xmlStructure.getCharacter();
	        			for(Character xmlCharacter : xmlCharacters) {
	        				DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
	        				character.setProperty("type", xmlCharacter.getCharacterType());
	        				character.setProperty("constraint", xmlCharacter.getConstraint());
	        				character.setProperty("constraintId", xmlCharacter.getConstraintId());
	        				character.setProperty("from", xmlCharacter.getFrom());
	        				character.setProperty("fromInclusive", xmlCharacter.getFromInclusive());
	        				character.setProperty("modifier", xmlCharacter.getModifier());
	        				character.setProperty("name" , xmlCharacter.getName());
	        				character.setProperty("to", xmlCharacter.getTo());
	        				character.setProperty("value", xmlCharacter.getValue());
							character.setProperty("unit", xmlCharacter.getUnit());
							character.setProperty("fromUnit", xmlCharacter.getFromUnit());
							character.setProperty("toUnit", xmlCharacter.getToUnit());
							character.setProperty("upperRestricted", xmlCharacter.getUpperRestricted());
							character.setProperty("toInclusive", xmlCharacter.getToInclusive());
							character.setProperty("relativeConstraint", xmlCharacter.getRelativeConstraint());
	        				
	        				structure.addTreatmentElement(character);
	        			}
	        		}
	        		
	        		for(Relation xmlRelation : xmlStatement.getRelation()) {
	        			DescriptionTreatmentElement relation = new DescriptionTreatmentElement(DescriptionType.RELATION);
	        			relation.setProperty("name", xmlRelation.getName());
	        			relation.setProperty("from",xmlRelation.getFrom());
	        			relation.setProperty("id",xmlRelation.getId());
	        			relation.setProperty("negation",xmlRelation.getNegation());
	        			relation.setProperty("to",xmlRelation.getTo());
	        			statement.addTreatmentElement(relation);
	        		}
	        	}
	        	result.add(treatment);
        	}
	    }
        return result;
	}
}
