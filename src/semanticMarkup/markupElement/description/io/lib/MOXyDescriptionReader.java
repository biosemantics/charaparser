package semanticMarkup.markupElement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import com.google.inject.name.Named;

import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

public class MOXyDescriptionReader implements IDescriptionReader {

	private String bindingsFile;
	
	public MOXyDescriptionReader(@Named("DescriptionReader_BindingsFile")String bindingsFile) {
		this.bindingsFile = bindingsFile;
	}
	
	private Unmarshaller getUnmarshaller() throws JAXBException {
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingsFile);
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {DescriptionsFile.class}, properties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); 
		return unmarshaller;
	}


	@Override
	public DescriptionsFileList read(@Named("DescriptionReader_InputDirectory")String inputDirectory) throws Exception {
		List<DescriptionsFile> descriptionsFiles = new LinkedList<DescriptionsFile>();
		File inputDirectoryFile = new File(inputDirectory);
		if(inputDirectoryFile.exists() && inputDirectoryFile.isDirectory()) {
			Unmarshaller unmarshaller = getUnmarshaller();
			for(File inputFile : inputDirectoryFile.listFiles()) {
				DescriptionsFile descriptionsFile = (DescriptionsFile)unmarshaller.unmarshal(inputFile);
				descriptionsFile.setFile(inputFile);
				descriptionsFiles.add(descriptionsFile);
			}
		} else {
			throw new IOException("Input directory does not exist or there is a name conflict with a file: " + 
					inputDirectory);
		}
		return new DescriptionsFileList(descriptionsFiles);
	}
	
}
