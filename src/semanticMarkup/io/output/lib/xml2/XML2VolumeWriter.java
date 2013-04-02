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
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * XML2VolumeWriter writes a list of treatments to an XML file
 * @author rodenhausen
 */
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
				for(ContainerTreatmentElement statement : description.getContainerTreatmentElements(DescriptionTreatmentElementType.STATEMENT.toString())) {
					DescriptionTreatmentElement statementElement = (DescriptionTreatmentElement)statement;
					Statement newStatement = new Statement();
					newStatement.setText(statementElement.getAttribute("text"));
					ArrayList<Structure> newStructureList = new ArrayList<Structure>();
				
					for(ContainerTreatmentElement structure : statementElement.getContainerTreatmentElements(DescriptionTreatmentElementType.STRUCTURE.toString())) {
						DescriptionTreatmentElement structureElement = (DescriptionTreatmentElement)structure;
						Structure newStructure = new Structure();
						newStructure.setConstraint(structureElement.getAttribute("constraint"));
						newStructure.setId(structureElement.getAttribute("id"));
						newStructure.setName(structureElement.getAttribute("name"));
						
						ArrayList<Character> newCharacterList = new ArrayList<Character>();
						for(TreatmentElement character : structureElement.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
							DescriptionTreatmentElement characterElement = (DescriptionTreatmentElement)character;
							Character newCharacter = new Character();
							newCharacter.setCharacterType(characterElement.getAttribute("type"));
							newCharacter.setConstraint(characterElement.getAttribute("constraint"));
							newCharacter.setConstraintId(characterElement.getAttribute("constraintId"));
							newCharacter.setFrom(characterElement.getAttribute("from"));
							newCharacter.setFromInclusive(characterElement.getAttribute("fromInclusive"));
							newCharacter.setModifier(characterElement.getAttribute("modifier"));
							newCharacter.setName(characterElement.getAttribute("name"));
							newCharacter.setTo(characterElement.getAttribute("to"));
							newCharacter.setValue(characterElement.getAttribute("value"));
							newCharacter.setUnit(characterElement.getAttribute("unit"));
							newCharacter.setFromUnit(characterElement.getAttribute("fromUnit"));
							newCharacter.setToUnit(characterElement.getAttribute("toUnit"));
							newCharacter.setUpperRestricted(characterElement.getAttribute("upperRestricted"));
							newCharacter.setToInclusive(characterElement.getAttribute("toInclusive"));
							newCharacter.setRelativeConstraint(characterElement.getAttribute("relativeConstraint"));
							newCharacterList.add(newCharacter);
						}
						newStructure.setCharacter(newCharacterList);
						newStructureList.add(newStructure);
					}
					newStatement.setStructure(newStructureList);
					
					ArrayList<Relation> newRelationList = new ArrayList<Relation>();
					for(TreatmentElement relation : statementElement.getTreatmentElements(DescriptionTreatmentElementType.RELATION.toString())) {
						DescriptionTreatmentElement relationElement = (DescriptionTreatmentElement)relation;
						
						Relation newRelation = new Relation();
						newRelation.setFrom(relationElement.getAttribute("from"));
						newRelation.setId(relationElement.getAttribute("id"));
						newRelation.setName(relationElement.getAttribute("name"));
						newRelation.setNegation(relationElement.getAttribute("negation"));
						newRelation.setTo(relationElement.getAttribute("to"));
						newRelation.setModifier(relationElement.getAttribute("modifier"));
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
