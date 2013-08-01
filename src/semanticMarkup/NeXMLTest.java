package semanticMarkup;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import semanticMarkup.markupElement.description.model.nexml.NeXMLDescriptionsFile;

public class NeXMLTest {

	/**
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		List<String> bindingFiles = new LinkedList<String>();
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//baseBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//neXMLBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model.nexml//neXMLBindings.xml");
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingFiles);
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {NeXMLDescriptionsFile.class}, jaxbContextProperties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		NeXMLDescriptionsFile descriptionsFile = (NeXMLDescriptionsFile)unmarshaller.unmarshal(new File("input//Swartz 2012.xml"));
		//System.out.println(descriptionsFile);
		/*for(Description description : descriptionsFile.getDescriptions()) {
			System.out.println(description.getText());
		}
		System.out.println(descriptionsFile.getDescriptions().size());*/
	}

}
