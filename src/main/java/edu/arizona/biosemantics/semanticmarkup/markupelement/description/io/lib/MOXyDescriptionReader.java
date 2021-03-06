package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;


import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;

public class MOXyDescriptionReader implements IDescriptionReader {

	private Unmarshaller unmarshaller;

	public MOXyDescriptionReader(@Named("DescriptionReader_BindingsFiles")List<String> bindingsFiles) throws JAXBException {
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingsFiles);
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {AbstractDescriptionsFile.class}, properties);
		this.unmarshaller = jaxbContext.createUnmarshaller(); 
	}

	@Override
	public DescriptionsFileList read(String inputDirectory) throws Exception {
		List<AbstractDescriptionsFile> descriptionsFiles = new LinkedList<AbstractDescriptionsFile>();
		File inputDirectoryFile = new File(inputDirectory);
		if(inputDirectoryFile.exists() && inputDirectoryFile.isDirectory()) {
			for(File inputFile : inputDirectoryFile.listFiles()) {
				AbstractDescriptionsFile descriptionsFile = (AbstractDescriptionsFile)unmarshaller.unmarshal(inputFile);
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
