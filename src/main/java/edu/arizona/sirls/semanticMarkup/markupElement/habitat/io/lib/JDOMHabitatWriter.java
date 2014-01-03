package edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.lib;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.DefaultJDOMFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMFactory;
import org.jdom2.Parent;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.IHabitatWriter;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.Treatment;


public class JDOMHabitatWriter implements IHabitatWriter {

	@Override
	public void write(HabitatsFileList habitatsFileList, String inputDirectory) throws Exception {
		for (HabitatsFile habitatsFile : habitatsFileList.getHabitatsFiles()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(habitatsFile.getFile());
			Element root = document.getRootElement();
			ElementFilter filter = new ElementFilter("habitat");

			List<Element> elements = new LinkedList<Element>();
			for (Element element : root.getDescendants(filter))
				elements.add(element);

			//for the xml format it is assumed one file to contain only one treatment
			Treatment treatment = habitatsFile.getTreatments().get(0);
			
			for (int i = 0; i < elements.size(); i++) {
				Element currentElement = elements.get(i);
				Parent parent = currentElement.getParent();
				List<Content> content = parent.getContent();
				
				for (int j = 0; j < content.size(); j++) {
					Content currentContent = content.get(j);
					if(currentContent.equals(currentElement)) {
						parent.removeContent(j);
						//if the last habitat element, replace the last one with the new habitat value
						if(i == elements.size()-1) {
							JDOMFactory jdomFactory = new DefaultJDOMFactory();
							Element element = jdomFactory.element("habitat");
							element.setText(treatment.getHabitat().getText());
							parent.addContent(j, element);
						}
						break;
					}
				}
			}
				
			XMLOutputter output = new XMLOutputter();
			output.output(document, new FileOutputStream(habitatsFile.getFile()));
		}
	}
}
