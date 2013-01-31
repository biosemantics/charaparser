package semanticMarkup.io.input.lib.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.io.input.IVolumeReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class StandardVolumeReader implements IVolumeReader {

	private String sourceFiles;
	private Set<String> selectedSources;

	@Inject
	public StandardVolumeReader(@Named("StandardVolumeReader_Sourcefiles")String sourceFiles, 
			@Named("selectedSources") Set<String> selectedSources) {
		this.sourceFiles = sourceFiles;
		this.selectedSources = selectedSources;
	}
	
	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> treatments = new ArrayList<Treatment>();
		
		String file;
		File folder = new File(sourceFiles);
		if(folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			
			String previousTreatmentId = "";
			Treatment previousTreatment = null;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					file = listOfFiles[i].getName();
					file = file.replace(".xml", "");
					if(selectedSources.contains(file)) {
						String[] sourceIds = file.split(".txt-");
						String treatmentId = String.valueOf(Integer.valueOf(sourceIds[0])-1);
						//String statementId = sourceIds[1];
						
						if(!previousTreatmentId.equals(treatmentId)) {
							if(previousTreatment!=null)
								treatments.add(previousTreatment);
							previousTreatmentId = treatmentId;
							previousTreatment = new Treatment(treatmentId);
						}
						createStatement(previousTreatment, listOfFiles[i]);
					}
				}
			}
			treatments.add(previousTreatment);
		}
		
		return treatments;
	}

	private void createStatement(Treatment treatment, File file) throws Exception {
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.input.lib.eval");
        Unmarshaller u = jc.createUnmarshaller();
        Description description = (Description)u.unmarshal(file);
        
        Statement statement = description.getStatement();
        transformStatement(treatment, statement, file);        
	}

	private void transformStatement(Treatment treatment, Statement xmlStatement, File file) {
		if(!treatment.containsContainerTreatmentElement("description")) 
			treatment.addTreatmentElement(new ContainerTreatmentElement("description"));
		ContainerTreatmentElement description = treatment.getContainerTreatmentElement("description");
		
		DescriptionTreatmentElement statement = new DescriptionTreatmentElement(DescriptionType.STATEMENT);
		statement.setProperty("id", xmlStatement.getId());
		statement.setProperty("source", file.getName().split(".xml")[0]);
		description.addTreatmentElement(statement);
		ArrayList<Structure> xmlStructures = xmlStatement.getStructure();
		ArrayList<Relation> xmlRelations = xmlStatement.getRelation();
	
		for(Structure xmlStructure : xmlStructures) {
			DescriptionTreatmentElement structure = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
			String id = xmlStructure.getId();
			//id = id == null ? null : id.substring(1);
			structure.setProperty("id", id);
			structure.setProperty("name", xmlStructure.getName());
			structure.setProperty("constraint", xmlStructure.getConstraint());
			
			ArrayList<Character> xmlCharacters = xmlStructure.getCharacter();
			for(Character xmlCharacter : xmlCharacters) {
				DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
				character.setProperty("name", xmlCharacter.getName());
				character.setProperty("constraint", xmlCharacter.getConstraint());
				character.setProperty("type", xmlCharacter.getCharacterType());
				String constraintId = xmlCharacter.getConstraintId();
				//constraintId = constraintId == null ? null : constraintId.substring(1);
				character.setProperty("constraintId", constraintId);
				character.setProperty("from", xmlCharacter.getFrom());
				character.setProperty("fromInclusive", xmlCharacter.getFromInclusive());
				character.setProperty("modifier", xmlCharacter.getModifier());
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
			statement.addTreatmentElement(structure);
		}
		
		for(Relation xmlRelation : xmlRelations) {
			DescriptionTreatmentElement relation = new DescriptionTreatmentElement(DescriptionType.RELATION);
			String id = xmlRelation.getId();
			//id = id == null ? null : id.substring(1);
			relation.setProperty("id", id);
			relation.setProperty("name", xmlRelation.getName());
			String from = xmlRelation.getFrom();
			//from = from == null ? null : from.substring(1);
			relation.setProperty("from", from);
			String to = xmlRelation.getTo();
			//to = to == null ? null : to.substring(1);
			relation.setProperty("to", to);
			relation.setProperty("negation", xmlRelation.getNegation());
			statement.addTreatmentElement(relation);
		}
	}
}
