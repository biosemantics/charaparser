package semanticMarkup.io.input.lib.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
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
	        		DescriptionTreatmentElement statement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STATEMENT);
	        		statement.setAttribute("text", xmlStatement.getText());
	        		statement.setAttribute("source", xmlStatement.getSource());
	        		description.addTreatmentElement(statement);
	        		
	        		ArrayList<Structure> xmlStructures = xmlStatement.getStructure();
	        		for(Structure xmlStructure : xmlStructures) {
	        			DescriptionTreatmentElement structure = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STRUCTURE);
	        			structure.setAttribute("id", xmlStructure.getId());
	        			structure.setAttribute("constraint", xmlStructure.getConstraint());
	        			structure.setAttribute("name", xmlStructure.getName());
	        			statement.addTreatmentElement(structure);
	        			
	        			ArrayList<Character> xmlCharacters = xmlStructure.getCharacter();
	        			for(Character xmlCharacter : xmlCharacters) {
	        				DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionTreatmentElementType.CHARACTER);
	        				character.setAttribute("type", xmlCharacter.getCharacterType());
	        				character.setAttribute("constraint", xmlCharacter.getConstraint());
	        				character.setAttribute("constraintId", xmlCharacter.getConstraintId());
	        				character.setAttribute("from", xmlCharacter.getFrom());
	        				character.setAttribute("fromInclusive", xmlCharacter.getFromInclusive());
	        				character.setAttribute("modifier", xmlCharacter.getModifier());
	        				character.setAttribute("name" , xmlCharacter.getName());
	        				character.setAttribute("to", xmlCharacter.getTo());
	        				character.setAttribute("value", xmlCharacter.getValue());
							character.setAttribute("unit", xmlCharacter.getUnit());
							character.setAttribute("fromUnit", xmlCharacter.getFromUnit());
							character.setAttribute("toUnit", xmlCharacter.getToUnit());
							character.setAttribute("upperRestricted", xmlCharacter.getUpperRestricted());
							character.setAttribute("toInclusive", xmlCharacter.getToInclusive());
							character.setAttribute("relativeConstraint", xmlCharacter.getRelativeConstraint());
	        				
	        				structure.addTreatmentElement(character);
	        			}
	        		}
	        		
	        		for(Relation xmlRelation : xmlStatement.getRelation()) {
	        			DescriptionTreatmentElement relation = new DescriptionTreatmentElement(DescriptionTreatmentElementType.RELATION);
	        			relation.setAttribute("name", xmlRelation.getName());
	        			relation.setAttribute("from",xmlRelation.getFrom());
	        			relation.setAttribute("id",xmlRelation.getId());
	        			relation.setAttribute("negation",xmlRelation.getNegation());
	        			relation.setAttribute("to",xmlRelation.getTo());
	        			statement.addTreatmentElement(relation);
	        		}
	        	}
	        	result.add(treatment);
        	}
	    }
        return result;
	}
}
