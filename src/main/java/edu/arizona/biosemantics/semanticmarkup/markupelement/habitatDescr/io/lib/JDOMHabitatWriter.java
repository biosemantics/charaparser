package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.lib;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;

public class JDOMHabitatWriter implements IHabitatWriter {

	@Override
	public void write(HabitatsFileList habitatsFileList, String outputDirectory) throws Exception {
		for (HabitatsFile habitatsFile : habitatsFileList.getHabitatsFiles()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(habitatsFile.getFile());
			Element root = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");

			List<Element> elements = new LinkedList<Element>();
			for (Element element : root.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("habitat")) {
					elements.add(element);
				}
			}

			//for the xml format it is assumed one file to contain only one treatment
			Treatment treatment = habitatsFile.getTreatments().get(0);
			
			for(int i=0; i< elements.size(); i++) {
				Habitat habitat = treatment.getHabitats().get(i);
				Element currentElement = elements.get(i);
				currentElement.setText("");
				for(Statement statement : habitat.getStatements()) {
					Element statementElement = new Element("statement");
					Element textElement = new Element("text");
					textElement.setText(statement.getText());
					statementElement.setAttribute("id", statement.getId());
					statementElement.addContent(textElement);
					
					for(BiologicalEntity be: statement.getBiologicalEntities()){
						Element beElement = new Element("biological_entity");
						beElement.setAttribute("name", be.getName());
						beElement.setAttribute("name_original", be.getNameOriginal());
						beElement.setAttribute("type", be.getType());
						for(Character ch: be.getCharacters()){
							Element chElement = new Element("character");
							chElement.setAttribute("name", ch.getName());
							chElement.setAttribute("value", ch.getValue());
							beElement.addContent(chElement);
						}
						statementElement.addContent(beElement);						
					}
					/*for(Value value: statement.getValues()){
						Element valueElement = new Element("value");
						valueElement.setText(value.getText());
						statementElement.addContent(valueElement);
					}*/
					currentElement.addContent(statementElement);
				}
			}
							
			XMLOutputter output = new XMLOutputter();
			output.output(document, new FileOutputStream(habitatsFile.getFile()));
		}
	}

}
