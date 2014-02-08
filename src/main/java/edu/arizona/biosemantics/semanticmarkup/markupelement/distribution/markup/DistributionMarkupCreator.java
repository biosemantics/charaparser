package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.IDistributionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.IDistributionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform.IDistributionTransformer;

public class DistributionMarkupCreator implements IDistributionMarkupCreator {

	private IDistributionReader reader;
	private IDistributionTransformer distributionTransformer;
	private IDistributionWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public DistributionMarkupCreator(@Named("DistributionMarkupCreator_DistributionReader") IDistributionReader reader,	
			IDistributionTransformer distributionTransformer,
			@Named("DistributionMarkupCreator_DistributionWriter") IDistributionWriter writer, 
			@Named("DistributionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.distributionTransformer = distributionTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public DistributionMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			DistributionsFileList distributionsFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + distributionTransformer.getClass());
			distributionTransformer.transform(distributionsFileList.getDistributionsFiles());
			
			/*for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
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
			}*/
			
			log(LogLevel.DEBUG, "writing result using " + writer.getClass());
			writer.write(distributionsFileList, outputDirectory);
			
			return new DistributionMarkupResult(distributionsFileList);
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
