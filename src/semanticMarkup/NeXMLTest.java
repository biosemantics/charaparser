package semanticMarkup;

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

import com.google.inject.name.Named;

import semanticMarkup.markupElement.description.io.lib.Binding;
import semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionReader;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;
import semanticMarkup.markupElement.description.model.nexml.NeXMLDescriptionsFile;

public class NeXMLTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		List<String> bindingFiles = new LinkedList<String>();
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//baseBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//neXMLBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model.nexml//neXMLBindings.xml");
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingFiles);
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {AbstractDescriptionsFile.class}, jaxbContextProperties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		AbstractDescriptionsFile descriptionsFile = (AbstractDescriptionsFile)unmarshaller.unmarshal(new File("input//Swartz 2012.xml"));
		
		
		MOXyBinderDescriptionReader reader = new MOXyBinderDescriptionReader(bindingFiles, new HashMap<File, Binding>());
		DescriptionsFileList fileList = reader.read("input");
		
		//System.out.println(descriptionsFile);
		/*for(Description description : descriptionsFile.getDescriptions()) {
			System.out.println(description.getText());
		}
		System.out.println(descriptionsFile.getDescriptions().size());*/
	}

}
