package semanticMarkup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;

public class NexTest {

	/**
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE, "resources//io//bindings//nexBindings.xml");
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {DescriptionsFile.class}, jaxbContextProperties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		DescriptionsFile descriptionsFile = (DescriptionsFile)unmarshaller.unmarshal(new File("input//Swartz 2012.xml"));
		//System.out.println(descriptionsFile);
		/*for(Description description : descriptionsFile.getDescriptions()) {
			System.out.println(description.toString());
		}*/
	}

}
