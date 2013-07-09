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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.io.IDescriptionWriter;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

public class MOXyBinderDescriptionReaderWriter implements IDescriptionReader, IDescriptionWriter {

	private Binder<Node> binder;
	private Map<File, Document> documents = new HashMap<File, Document>();
	
	public MOXyBinderDescriptionReaderWriter(String bindingsFile) throws JAXBException {
		Map<String, Object> jaxbContextProperties = new HashMap<String, Object>(1);
		jaxbContextProperties.put(JAXBContextProperties.OXM_METADATA_SOURCE , bindingsFile);
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { Description.class }, jaxbContextProperties);
		this.binder = jaxbContext.createBinder();
	}

	@Override
	public DescriptionsFileList read(String inputDirectory) throws Exception {
		List<DescriptionsFile> descriptionsFiles = new LinkedList<DescriptionsFile>();
		File inputDirectoryFile = new File(inputDirectory);
		if(inputDirectoryFile.exists() && inputDirectoryFile.isDirectory()) {
			
			for(File inputFile : inputDirectoryFile.listFiles()) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db = dbf.newDocumentBuilder();
		        Document document = db.parse(inputFile);
				documents.put(inputFile, document);
		        
		        DescriptionsFile descriptionsFile = (DescriptionsFile) binder.unmarshal(document);
		        descriptionsFile.setFile(inputFile);
				descriptionsFiles.add(descriptionsFile);
			}
		} else {
			throw new IOException("Input directory does not exist or there is a name conflict with a file: " + 
					inputDirectory);
		}
		return new DescriptionsFileList(descriptionsFiles);
	}

	@Override
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws Exception {
		File outDirectoryFile = new File(writeDirectory);
		if(outDirectoryFile.exists() && outDirectoryFile.isDirectory()) {			
			for(DescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				if(documents.containsKey(descriptionsFile.getFile())) {
					Document document = documents.get(descriptionsFile.getFile());
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
