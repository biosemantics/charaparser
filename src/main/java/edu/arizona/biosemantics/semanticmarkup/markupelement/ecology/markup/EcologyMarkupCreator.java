/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.transform.IEcologyTransformer;
/**
 * @author updates
 *
 */
public class EcologyMarkupCreator implements IEcologyMarkupCreator{

	private IEcologyReader reader;
	private IEcologyTransformer ecologyTransformer;
	private IEcologyWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public EcologyMarkupCreator(@Named("EcologyMarkupCreator_EcologyReader") IEcologyReader reader,	
			IEcologyTransformer ecologyTransformer,
			@Named("EcologyMarkupCreator_EcologyWriter") IEcologyWriter writer, 
			@Named("EcologyReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.ecologyTransformer = ecologyTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public EcologyMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			EcologyFileList ecologyFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + ecologyTransformer.getClass());
			ecologyTransformer.transform(ecologyFileList.getEcologyFiles());
			
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
			writer.write(ecologyFileList, outputDirectory);
			
			return new EcologyMarkupResult(ecologyFileList);
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
