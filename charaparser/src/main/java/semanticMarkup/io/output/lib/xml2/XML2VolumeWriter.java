package semanticMarkup.io.output.lib.xml2;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class XML2VolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public XML2VolumeWriter(@Named("Run_OutFile") String filePath) {
		super(filePath);
	}
	
	@Override
	public void write(List<Treatment> treatments) throws Exception {
		FileOutputStream outputStream = new FileOutputStream(new File(filePath + ".xml"));
		
		Treatments treatmentContainer = transformTreatments(treatments);
		
		//Class<?>[] classes = {Treatments.class};
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.output.lib.xml2");	    
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(treatmentContainer, outputStream);
	}
	
	private Treatments transformTreatments(List<Treatment> treatments) {
		Treatments result = new Treatments();
		ArrayList<semanticMarkup.io.output.lib.xml2.Treatment> treatmentList = new ArrayList<semanticMarkup.io.output.lib.xml2.Treatment>();
		for(Treatment treatment : treatments) {
			
			semanticMarkup.io.output.lib.xml2.Treatment t = new semanticMarkup.io.output.lib.xml2.Treatment();
			t.setNumber(treatment.getName());
			
			for(ContainerTreatmentElement description : treatment.getContainerTreatmentElements("description")) {
				
				Description newDescription = new Description();
				ArrayList<Statement> statements = new ArrayList<Statement>();
				for(ContainerTreatmentElement statement : description.getContainerTreatmentElements(DescriptionType.STATEMENT.toString())) {
					DescriptionTreatmentElement statementElement = (DescriptionTreatmentElement)statement;
					Statement newStatement = new Statement();
					newStatement.setText(statementElement.getProperty("text"));
					ArrayList<Structure> newStructureList = new ArrayList<Structure>();
				
					for(ContainerTreatmentElement structure : statementElement.getContainerTreatmentElements(DescriptionType.STRUCTURE.toString())) {
						DescriptionTreatmentElement structureElement = (DescriptionTreatmentElement)structure;
						Structure newStructure = new Structure();
						newStructure.setConstraint(structureElement.getProperty("constraint"));
						newStructure.setId(structureElement.getProperty("id"));
						newStructure.setName(structureElement.getProperty("name"));
						
						ArrayList<Character> newCharacterList = new ArrayList<Character>();
						for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionType.CHARACTER.toString())) {
							DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
							Character newCharacter = new Character();
							newCharacter.setCharacterType(characterElement.getProperty("type"));
							newCharacter.setConstraint(characterElement.getProperty("constraint"));
							newCharacter.setConstraintId(characterElement.getProperty("constraintId"));
							newCharacter.setFrom(characterElement.getProperty("from"));
							newCharacter.setFromInclusive(characterElement.getProperty("fromInclusive"));
							newCharacter.setModifier(characterElement.getProperty("modifier"));
							newCharacter.setName(characterElement.getProperty("name"));
							newCharacter.setTo(characterElement.getProperty("to"));
							newCharacter.setValue(characterElement.getProperty("value"));
							newCharacter.setUnit(characterElement.getProperty("unit"));
							newCharacter.setFromUnit(characterElement.getProperty("fromUnit"));
							newCharacter.setToUnit(characterElement.getProperty("toUnit"));
							newCharacter.setUpperRestricted(characterElement.getProperty("upperRestricted"));
							newCharacter.setToInclusive(characterElement.getProperty("toInclusive"));
							newCharacter.setRelativeConstraint(characterElement.getProperty("relativeConstraint"));
							newCharacterList.add(newCharacter);
						}
						newStructure.setCharacter(newCharacterList);
						newStructureList.add(newStructure);
					}
					newStatement.setStructure(newStructureList);
					
					ArrayList<Relation> newRelationList = new ArrayList<Relation>();
					for(TreatmentElement relation : statementElement.getTreatmentElements(DescriptionType.RELATION.toString())) {
						DescriptionTreatmentElement relationElement = (DescriptionTreatmentElement)relation;
						
						Relation newRelation = new Relation();
						newRelation.setFrom(relationElement.getProperty("from"));
						newRelation.setId(relationElement.getProperty("id"));
						newRelation.setName(relationElement.getProperty("name"));
						newRelation.setNegation(relationElement.getProperty("negation"));
						newRelation.setTo(relationElement.getProperty("to"));
						newRelation.setModifier(relationElement.getProperty("modifier"));
						//relation doesnt have modifier?
						//newRelation.setTo(relationElement.getProperty("modifier"));
						
						newRelationList.add(newRelation);
					}
					newStatement.setRelation(newRelationList);
					statements.add(newStatement);
				}
				newDescription.setStatement(statements);
				t.setDescription(newDescription);
			}
			treatmentList.add(t);
		} 
		result.setTreatment(treatmentList);
		return result;
	}

	public static void main(String[] args) throws Exception {
		/*String filePath = "test";
		
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment1 = new Treatment("treatment1");
		Treatment treatment2 = new Treatment("treatment2");
		Treatment treatment3 = new Treatment("treatment3");
		treatments.add(treatment1);
		treatments.add(treatment2);
		treatments.add(treatment3);
		treatment1.addTreatmentElement(new ValueTreatmentElement("habitat", "value"));
		treatment1.addTreatmentElement(new ValueTreatmentElement("author", "value2"));
		treatment1.addTreatmentElement(new ValueTreatmentElement("description", "text"));
		ContainerTreatmentElement references = new ContainerTreatmentElement("references");
		references.addTreatmentElement(new ValueTreatmentElement("ref1", "http://..."));
		references.addTreatmentElement(new ValueTreatmentElement("ref2", "http://2..."));
		treatment1.addTreatmentElement(references);
		
		FileOutputStream outputStream = new FileOutputStream(new File(filePath));
		
		Treatments treatmentContainer = new Treatments(treatments);
		
		Class<?>[] classes = { ContainerTreatmentElement.class, Treatment.class, TreatmentElement.class, ValueTreatmentElement.class, Treatments.class };
		JAXBContext jc = JAXBContext.newInstance(classes);
		//JAXBContext jc = JAXBContext.newInstance("semanticMarkup.core");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(treatmentContainer, outputStream);*/
	}
}
