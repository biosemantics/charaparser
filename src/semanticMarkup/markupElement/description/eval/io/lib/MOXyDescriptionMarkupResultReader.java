package semanticMarkup.markupElement.description.eval.io.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import com.google.inject.Inject;

import semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
import semanticMarkup.markupElement.description.eval.model.Description;
import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;

public class MOXyDescriptionMarkupResultReader implements IDescriptionMarkupResultReader {

	private Unmarshaller unmarshaller = null;
	
	@Inject
	public MOXyDescriptionMarkupResultReader(List<String> bindingFiles) throws JAXBException {
		Map<String, Object> props = new HashMap<String, Object>(1);
		props.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingFiles);
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { Description.class}, props);
		unmarshaller = jaxbContext.createUnmarshaller();
	}

	@Override
	public DescriptionMarkupResult read(String inputDirectory) throws JAXBException {
		List<Description> descriptions = new LinkedList<Description>();
		File inputDirectoryFile = new File(inputDirectory);
		for(File inputFile : inputDirectoryFile.listFiles()) {
			Description description = (Description) unmarshaller.unmarshal(inputFile);
			description.setSource(inputFile.getName());
			descriptions.add(description);
		}
	
		DescriptionMarkupResult result = new DescriptionMarkupResult(descriptions);
		return result;
	}	
}
