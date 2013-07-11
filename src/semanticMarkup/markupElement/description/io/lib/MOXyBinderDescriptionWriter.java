package semanticMarkup.markupElement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.io.IDescriptionWriter;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MOXyBinderDescriptionWriter implements IDescriptionWriter {

	private Map<File, Binding> fileDocumentMappings;
	private JAXBContext jaxbContext;
	
	@Inject
	public MOXyBinderDescriptionWriter(@Named("DescriptionReader_BindingsFiles")List<String> bindingsFiles, 
			@Named("MOXyBinderDescriptionReaderWriter_FileDocumentMappings")Map<File, Binding> fileDocumentMappings) 
			throws JAXBException {
		this.fileDocumentMappings = fileDocumentMappings;
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE , bindingsFiles);
		jaxbContext = JAXBContextFactory.createContext(new Class[] {Description.class}, jaxbContextProperties);
	}

	@Override
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws Exception {
		File outDirectoryFile = new File(writeDirectory);
		if(outDirectoryFile.exists() && outDirectoryFile.isDirectory()) {			
			for(DescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				if(fileDocumentMappings.containsKey(descriptionsFile.getFile())) {
					Binding binding = fileDocumentMappings.get(descriptionsFile.getFile());
					Binder<Node> binder = binding.getBinder();
					Document document = binding.getDocument();
					binder.updateXML(descriptionsFile);
					
					File outputFile = new File(writeDirectory + File.separator + descriptionsFile.getFile().getName());
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
			        Transformer transformer = transformerFactory.newTransformer();
			        transformer.transform(new DOMSource(document), new StreamResult(outputFile));
				} else {
					this.log(LogLevel.ERROR, "There is a descriptionsFile without a corresponding DOM Document");
				}
			}
		} else {
			throw new IOException("Output directory does not exist or there is a name conflict with a file: " + 
					writeDirectory);
		}
	}

}
