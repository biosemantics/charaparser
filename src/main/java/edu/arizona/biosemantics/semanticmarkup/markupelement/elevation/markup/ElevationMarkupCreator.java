package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.transform.IElevationTransformer;

public class ElevationMarkupCreator implements IElevationMarkupCreator {

	private IElevationReader reader;
	private IElevationTransformer elevationTransformer;
	private IElevationWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public ElevationMarkupCreator(@Named("ElevationMarkupCreator_ElevationReader") IElevationReader reader,	
			IElevationTransformer elevationTransformer,
			@Named("ElevationMarkupCreator_ElevationWriter") IElevationWriter writer, 
			@Named("ElevationReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.elevationTransformer = elevationTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public ElevationMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			ElevationsFileList elevationsFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + elevationTransformer.getClass());
			elevationTransformer.transform(elevationsFileList.getElevationsFiles());
			
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
			writer.write(elevationsFileList, outputDirectory);
			
			return new ElevationMarkupResult(elevationsFileList);
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
