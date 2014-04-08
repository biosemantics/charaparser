/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.lib;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Ecology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Treatment;


/**
 * @author Hong Cui
 *
 */
public class JDOMEcologyWriter implements IEcologyWriter{
	
	@Override
	public void write(EcologyFileList elevationsFileList, String outputDirectory) throws Exception {
		for (EcologyFile ecologyFile : elevationsFileList.getEcologyFiles()) {
			SAXBuilder builder = new SAXBuilder();
			//if(ecologyFile.getFile().getAbsolutePath().contains("639_154")) {
			//	System.out.println("here");
			//}
			
			Document document = builder.build(ecologyFile.getFile());
			Element root = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");

			List<Element> elements = new LinkedList<Element>();
			for (Element element : root.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("ecology")) {
					elements.add(element);
				}
			}

			//for the xml format it is assumed one file to contain only one treatment
			Treatment treatment = ecologyFile.getTreatments().get(0);
			
			for(int i=0; i< elements.size(); i++) {
				Ecology ecology = treatment.getEcology().get(i);
				Element currentElement = elements.get(i);
				currentElement.setText("");
				for(Statement statement : ecology.getStatements()) {
					Element statementElement = new Element("statement");
					Element textElement = new Element("text");
					textElement.setText(statement.getText());
					statementElement.setAttribute("id", statement.getId());
					statementElement.addContent(textElement);
					currentElement.addContent(statementElement);
				}
			}
							
			XMLOutputter output = new XMLOutputter();
			output.output(document, new FileOutputStream(ecologyFile.getFile()));
		}
	}

}
