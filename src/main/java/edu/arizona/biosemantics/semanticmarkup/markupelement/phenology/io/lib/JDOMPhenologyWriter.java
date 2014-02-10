package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.lib;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class JDOMPhenologyWriter implements IPhenologyWriter {

	@Override
	public void write(PhenologiesFileList phenologiesFileList, String outputDirectory) throws Exception {
		for (PhenologiesFile phenologiesFile : phenologiesFileList.getPhenologiesFiles()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(phenologiesFile.getFile());
			Element root = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");

			List<Element> elements = new LinkedList<Element>();
			for (Element element : root.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("phenology")) {
					elements.add(element);
				}
			}

			//for the xml format it is assumed one file to contain only one treatment
			Treatment treatment = phenologiesFile.getTreatments().get(0);
			
			for(int i=0; i< elements.size(); i++) {
				Phenology phenology = treatment.getPhenologies().get(i);
				Element currentElement = elements.get(i);
				currentElement.setText("");
				for(Statement statement : phenology.getStatements()) {
					Element statementElement = new Element("statement");
					Element textElement = new Element("text");
					textElement.setText(statementElement.getText());
					statementElement.setAttribute("id", statement.getId());
					statementElement.addContent(textElement);
					currentElement.addContent(statementElement);
				}
			}
							
			XMLOutputter output = new XMLOutputter();
			output.output(document, new FileOutputStream(phenologiesFile.getFile()));
		}
	}

}
