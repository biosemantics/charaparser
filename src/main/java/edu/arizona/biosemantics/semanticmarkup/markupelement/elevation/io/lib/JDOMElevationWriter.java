package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.lib;

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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;

public class JDOMElevationWriter implements IElevationWriter {

	@Override
	public void write(ElevationsFileList elevationsFileList, String outputDirectory) throws Exception {
		for (ElevationsFile elevationsFile : elevationsFileList.getElevationsFiles()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(elevationsFile.getFile());
			Element root = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");

			List<Element> elements = new LinkedList<Element>();
			for (Element element : root.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("elevation")) {
					elements.add(element);
				}
			}

			//for the xml format it is assumed one file to contain only one treatment
			Treatment treatment = elevationsFile.getTreatments().get(0);

			for(int i=0; i< elements.size(); i++) {
				Elevation elevation = treatment.getElevations().get(i);
				Element currentElement = elements.get(i);
				currentElement.setText("");
				for(Statement statement : elevation.getStatements()) {
					Element statementElement = new Element("statement");
					Element textElement = new Element("text");
					textElement.setText(statement.getText());
					statementElement.setAttribute("id", statement.getId());
					statementElement.addContent(textElement);

					for(BiologicalEntity be: statement.getBiologicalEntities()){
						Element beElement = new Element("biological_entity");
						beElement.setAttribute("id", be.getId() );
						beElement.setAttribute("name", be.getName());
						beElement.setAttribute("name_original", be.getNameOriginal());
						beElement.setAttribute("type", be.getType());
						for(Character ch: be.getCharacters()){
							Element chElement = new Element("character");
							chElement.setAttribute("name", ch.getName());
							if(ch.getCharType()!=null)  chElement.setAttribute("char_type", ch.getCharType());
							if(ch.getUpperRestricted()!=null) chElement.setAttribute("upper_restricted", ch.getUpperRestricted());
							if(ch.getValue()!=null) chElement.setAttribute("value", ch.getValue());
							if(ch.getUnit()!=null) chElement.setAttribute("unit",ch.getUnit());
							if(ch.getModifier()!=null) chElement.setAttribute("modifier", ch.getModifier());
							if(ch.getTo()!=null) chElement.setAttribute("to", ch.getTo());
							if(ch.getToUnit()!=null) chElement.setAttribute("to_unit",ch.getToUnit());
							if(ch.getToInclusive()!=null) chElement.setAttribute("to_inclusive",ch.getToInclusive() );
							if(ch.getToModifier()!=null) chElement.setAttribute("to_modifier", ch.getToModifier());

							if(ch.getFrom()!=null) chElement.setAttribute("from", ch.getFrom() );
							if(ch.getFromUnit()!=null) chElement.setAttribute("from_unit",ch.getFromUnit() );
							if(ch.getFromInclusive()!=null) chElement.setAttribute("from_inclusive",ch.getFromInclusive() );
							if(ch.getFromModifier()!=null) chElement.setAttribute("from_modifier", ch.getFromModifier());

							if(ch.getConstraint() != null && !ch.getConstraint().isEmpty())
								chElement.setAttribute("constraint", ch.getConstraint());

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
			output.output(document, new FileOutputStream(elevationsFile.getFile()));
		}
	}

}