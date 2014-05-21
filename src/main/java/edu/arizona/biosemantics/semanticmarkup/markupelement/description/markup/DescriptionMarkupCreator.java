package edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Meta;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.IDescriptionTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.TransformationReport;

/**
 * CharaParserMarkupCreator creates a markup by reading treatments, transforming them using a TreatmentTransformerChain and writing them out
 * @author thomas rodenhausen
 */
public class DescriptionMarkupCreator implements IDescriptionMarkupCreator {

	private IDescriptionReader reader;
	private IDescriptionTransformer descriptionTransformer;
	private IDescriptionWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	private String etcUser;
	
	@Inject
	public DescriptionMarkupCreator(@Named("DescriptionMarkupCreator_DescriptionReader") IDescriptionReader reader,	
			IDescriptionTransformer descriptionTransformer,
			@Named("DescriptionMarkupCreator_DescriptionWriter") IDescriptionWriter writer, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory, 
			@Named("EtcUser")String etcUser) {
		this.reader = reader;
		this.descriptionTransformer = descriptionTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
		this.etcUser = etcUser;
	}
	
	public DescriptionMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			DescriptionsFileList descriptionsFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + descriptionTransformer.getClass());
			TransformationReport report = descriptionTransformer.transform(descriptionsFileList.getDescriptionsFiles());
			
			for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
				Meta meta = descriptionsFile.getMeta();
				if(meta == null)
					meta = new Meta();
				Processor processor = new Processor();
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				processor.setDate(dateFormat.format(new Date()));
				processor.setOperator(etcUser);
				Resource resource = new Resource();
				resource.setName(report.getGlossaryType());
				resource.setType("OTO Glossary");
				resource.setVersion(report.getGlossaryVersion());
				Software software = new Software();
				software.setName("CharaParser");
				software.setType("Semantic Markup");
				software.setVersion(report.getCharaparserVersion());
				processor.setSoftware(software);
				processor.setResource(resource);
				
				meta.addProcessor(processor);
				descriptionsFile.setMeta(meta);
			}
			
			log(LogLevel.DEBUG, "writing result using " + writer.getClass());
			writer.write(descriptionsFileList, outputDirectory);
			
			return new DescriptionMarkupResult(descriptionsFileList);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem reading, transforming or writing treatments", e);
		}
		return null;
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
}

