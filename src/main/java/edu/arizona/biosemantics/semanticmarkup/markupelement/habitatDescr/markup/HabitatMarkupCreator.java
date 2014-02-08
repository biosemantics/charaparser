package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform.IHabitatTransformer;

public class HabitatMarkupCreator implements IHabitatMarkupCreator {

	private IHabitatReader reader;
	private IHabitatTransformer habitatTransformer;
	private IHabitatWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public HabitatMarkupCreator(@Named("HabitatMarkupCreator_HabitatReader") IHabitatReader reader,	
			IHabitatTransformer habitatTransformer,
			@Named("HabitatMarkupCreator_HabitatWriter") IHabitatWriter writer, 
			@Named("HabitatReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.habitatTransformer = habitatTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public HabitatMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			HabitatsFileList habitatsFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + habitatTransformer.getClass());
			habitatTransformer.transform(habitatsFileList.getHabitatsFiles());
			
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
			writer.write(habitatsFileList, outputDirectory);
			
			return new HabitatMarkupResult(habitatsFileList);
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
