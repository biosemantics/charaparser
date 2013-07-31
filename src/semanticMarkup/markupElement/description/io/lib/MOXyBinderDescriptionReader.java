package semanticMarkup.markupElement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MOXyBinderDescriptionReader implements IDescriptionReader {

	private Map<File, Binding> fileDocumentMappings;
	private JAXBContext jaxbContext;
	private DocumentBuilder documentBuilder;
	
	@Inject
	public MOXyBinderDescriptionReader(@Named("DescriptionReader_BindingsFiles")List<String> bindingsFiles,
			@Named("MOXyBinderDescriptionReaderWriter_FileDocumentMappings")Map<File, Binding> fileDocumentMappings)
			throws JAXBException, ParserConfigurationException {
		this.fileDocumentMappings = fileDocumentMappings;
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE , bindingsFiles);
		this.jaxbContext = JAXBContextFactory.createContext(new Class[] {Description.class}, jaxbContextProperties);
		
		this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();;
	}

	@Override
	public DescriptionsFileList read(String inputDirectory) throws Exception {
		List<AbstractDescriptionsFile> descriptionsFiles = new LinkedList<AbstractDescriptionsFile>();
		File inputDirectoryFile = new File(inputDirectory);
		if(inputDirectoryFile.exists() && inputDirectoryFile.isDirectory()) {			
			for(File inputFile : inputDirectoryFile.listFiles()) {
				try {
			        Document document = documentBuilder.parse(inputFile);
			        Binder<Node> binder = jaxbContext.createBinder();
			        AbstractDescriptionsFile descriptionsFile = (AbstractDescriptionsFile) binder.unmarshal(document);
			        descriptionsFile.setFile(inputFile);
					descriptionsFiles.add(descriptionsFile);
				
					fileDocumentMappings.put(inputFile, new Binding(document, binder));
				} catch (Exception e) {
					log(LogLevel.ERROR, "Could not read input file " + inputFile.getAbsolutePath(), e);
				} 
			}
		} else {
			throw new IOException("Input directory does not exist or there is a name conflict with a file: " + 
					inputDirectory);
		}
		return new DescriptionsFileList(descriptionsFiles);
	}

}
