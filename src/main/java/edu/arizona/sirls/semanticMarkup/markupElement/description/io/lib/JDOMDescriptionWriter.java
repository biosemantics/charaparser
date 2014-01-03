package edu.arizona.sirls.semanticMarkup.markupElement.description.io.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.IDescriptionWriter;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Description;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Statement;


public class JDOMDescriptionWriter implements IDescriptionWriter {
	
	private SAXBuilder saxBuilder = new SAXBuilder();
	private XPathFactory xPathFactory= XPathFactory.instance();
	private String descriptionXPath = "/treatment/description";

	@Override
	public void write(DescriptionsFileList descriptionsFileList,
			String writeDirectory) throws Exception {
		File writeDirectoryFile = new File(writeDirectory);
		if(!writeDirectoryFile.exists()) 
			writeDirectoryFile.mkdirs();
		if(writeDirectoryFile.exists() && writeDirectoryFile.isDirectory()) {
			
			for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				File sourceFile = descriptionsFile.getFile();
				File outputFile = new File(writeDirectory + File.separator + descriptionsFile.getFile().getName());
				List<Description> descriptions = descriptionsFile.getDescriptions();
				
		        Document sourceFileDocument = saxBuilder.build(sourceFile);
				List<Element> sourceDescriptionElements = getSourceDescriptionElements(sourceFileDocument);
				
	            if(descriptions.size() != sourceDescriptionElements.size()) {
	            	log(LogLevel.ERROR, "There was obviously a problem somewhere");
	            	return;
	            } else {
	            	//for each description in the list, same id as the description element in the inputfile
    				//start the replacement procedure using JDOM
	            	for(int i=0; i<descriptions.size(); i++) {
	            		Element sourceDescriptionElement = sourceDescriptionElements.get(i);
	            		Description description = descriptions.get(i);
	            		replaceDescription(sourceDescriptionElement, description);
	            	}
	            	XMLOutputter output=new XMLOutputter();
	                output.output(sourceFileDocument, new FileOutputStream(outputFile));
	            }
			}
		} else {
			throw new IOException("Name conflict with a file having the same name as writeDirectory: " + 
				writeDirectory);
		}
	}

	private void replaceDescription(Element sourceDescriptionElement, Description description) {
		//use JDOM to do the details of getting things in here..
		for(Statement statement : description.getStatements()) {
			sourceDescriptionElement.addContent(new Element("statement"));
			//...
		}
	}

	private List<Element> getSourceDescriptionElements(Document sourceFileDocument) {
        Namespace namespace = Namespace.getNamespace("http://etc-project.org/");
        XPathExpression<Element> descriptionXPathExpression = 
        		xPathFactory.compile(descriptionXPath, Filters.element(), null, namespace);
        return descriptionXPathExpression.evaluate(sourceFileDocument);
	}

}
