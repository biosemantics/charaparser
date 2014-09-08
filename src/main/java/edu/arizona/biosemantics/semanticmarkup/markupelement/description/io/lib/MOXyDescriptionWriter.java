package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;

public class MOXyDescriptionWriter implements IDescriptionWriter {

	private Marshaller marshaller;
	
	@Inject
	public MOXyDescriptionWriter(@Named("DescriptionWriter_BindingsFiles")List<String> bindingsFiles) throws JAXBException {
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE , bindingsFiles);
		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] {AbstractDescriptionsFile.class}, properties);
		this.marshaller = jaxbContext.createMarshaller(); 
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.github.com/biosemantics " +
				"http://raw.githubusercontent.com/biosemantics/schemas/0.0.1/semanticMarkupOutput.xsd");
	}
		
	@Override
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws JAXBException, IOException {
		File writeDirectoryFile = new File(writeDirectory);
		if(!writeDirectoryFile.exists()) 
			writeDirectoryFile.mkdirs();
		if(writeDirectoryFile.exists() && writeDirectoryFile.isDirectory()) {
			for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				File sourceFile = descriptionsFile.getFile();
				marshaller.marshal(descriptionsFile, 
						new File(writeDirectory + File.separator + descriptionsFile.getFile().getName()));
			}
		} else {
			throw new IOException("Name conflict with a file having the same name as writeDirectory: " + 
				writeDirectory);
		}
	}	
}
