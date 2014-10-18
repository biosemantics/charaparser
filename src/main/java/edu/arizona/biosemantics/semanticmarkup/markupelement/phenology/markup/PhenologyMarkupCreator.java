package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform.IPhenologyTransformer;

public class PhenologyMarkupCreator implements IPhenologyMarkupCreator {

	private IPhenologyReader reader;
	private IPhenologyTransformer phenologyTransformer;
	private IPhenologyWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public PhenologyMarkupCreator(@Named("PhenologyMarkupCreator_PhenologyReader") IPhenologyReader reader,	
			IPhenologyTransformer phenologyTransformer,
			@Named("PhenologyMarkupCreator_PhenologyWriter") IPhenologyWriter writer, 
			@Named("PhenologyReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.phenologyTransformer = phenologyTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public PhenologyMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			PhenologiesFileList phenologiesFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + phenologyTransformer.getClass());
			phenologyTransformer.transform(phenologiesFileList.getPhenologiesFiles());
			
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
			writer.write(phenologiesFileList, outputDirectory);
			
			return new PhenologyMarkupResult(phenologiesFileList);
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

