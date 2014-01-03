package edu.arizona.sirls.semanticMarkup;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import edu.arizona.sirls.semanticMarkup.markupElement.description.io.lib.Binding;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionReader;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionWriter;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Meta;


public class zMOXyTest {

	public static void main(String[] args) throws Exception {
		//Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		List<String> bindingFiles = new LinkedList<String>();
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//baseBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//singleTreatmentDescriptionBindings.xml");
		//jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingFiles);
		//JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {AbstractDescriptionsFile.class}, jaxbContextProperties);
		//Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		//AbstractDescriptionsFile descriptionsFile = (AbstractDescriptionsFile)unmarshaller.unmarshal(new File("C:\\cptestin\\208.xml"));
		
		HashMap<File, Binding> fileDocumentMappings = new HashMap<File, Binding>();
		MOXyBinderDescriptionReader reader = new MOXyBinderDescriptionReader(bindingFiles, fileDocumentMappings);
		DescriptionsFileList fileList = reader.read("C:\\cptestin\\");
		
		//TODO modify stuff here
		Meta meta = new Meta();
		meta.setCharaparserVersion("version");
		fileList.getDescriptionsFiles().get(0).setMeta(meta);
		
		MOXyBinderDescriptionWriter writer = new MOXyBinderDescriptionWriter(bindingFiles, fileDocumentMappings);
		writer.write(fileList, "C:\\cptestout\\");
		
		
		//MOXyBinderDescriptionReader reader = new MOXyBinderDescriptionReader(bindingFiles, new HashMap<File, Binding>());
		//DescriptionsFileList fileList = reader.read("input");
		
	}
		
	
}
