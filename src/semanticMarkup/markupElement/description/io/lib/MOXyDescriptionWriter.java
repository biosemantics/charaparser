package semanticMarkup.markupElement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import semanticMarkup.markupElement.description.io.IDescriptionWriter;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

public class MOXyDescriptionWriter implements IDescriptionWriter {

	private String bindingsFile;
	
	public MOXyDescriptionWriter(String bindingsFile) {
		this.bindingsFile = bindingsFile;
	}
		
	@Override
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws JAXBException, IOException {
		File writeDirectoryFile = new File(writeDirectory);
		if(!writeDirectoryFile.exists()) 
			writeDirectoryFile.mkdirs();
		if(writeDirectoryFile.exists() && writeDirectoryFile.isDirectory()) {
			Marshaller marshaller = getMarshaller();
			for(DescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				File sourceFile = descriptionsFile.getFile();
				marshaller.marshal(descriptionsFile, 
						new File(writeDirectory + File.separator + descriptionsFile.getFile().getName()));
			}
		} else {
			throw new IOException("Name conflict with a file having the same name as writeDirectory: " + 
				writeDirectory);
		}
	}
	
	private Marshaller getMarshaller() throws JAXBException {
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE , "resources" + File.separator + "iplantOutputBindings.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {DescriptionsFile.class}, properties);
		Marshaller marshaller = jaxbContext.createMarshaller(); 
		return marshaller;
	}


	
}
