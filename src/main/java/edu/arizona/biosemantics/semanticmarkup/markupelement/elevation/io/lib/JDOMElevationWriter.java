package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.lib;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Statement;
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
					textElement.setText(statementElement.getText());
					statementElement.setAttribute("id", statement.getId());
					statementElement.addContent(textElement);
					currentElement.addContent(statementElement);
				}
			}
							
			XMLOutputter output = new XMLOutputter();
			output.output(document, new FileOutputStream(elevationsFile.getFile()));
		}
	}

}
