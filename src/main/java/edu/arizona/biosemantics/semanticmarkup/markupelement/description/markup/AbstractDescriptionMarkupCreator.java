package edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.IChunker;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Meta;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.TreatmentRoot;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.IDescriptionTransformer;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * CharaParserMarkupCreator creates a markup by reading treatments, transforming them using a TreatmentTransformerChain and writing them out
 * @author thomas rodenhausen
 */
public abstract class AbstractDescriptionMarkupCreator implements IDescriptionMarkupCreator {

	private IDescriptionReader reader;
	private List<IDescriptionTransformer> descriptionTransformers = new LinkedList<IDescriptionTransformer>();
	private IDescriptionWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public AbstractDescriptionMarkupCreator(@Named("DescriptionMarkupCreator_DescriptionReader") IDescriptionReader reader,	
			@Named("DescriptionMarkupCreator_DescriptionWriter") IDescriptionWriter writer, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
	}
	
	public DescriptionMarkupResult create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			DescriptionsFileList descriptionsFileList = reader.read(inputDirectory);
			
			for(IDescriptionTransformer transformer : descriptionTransformers) {
				log(LogLevel.DEBUG, "transform treatments using " + transformer.getClass());
				try {
					Processor processor = transformer.transform(descriptionsFileList.getDescriptionsFiles());
					for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
						Meta meta = descriptionsFile.getMeta();
						if(meta == null)
							meta = new Meta();
						meta.addProcessor(processor);
						descriptionsFile.setMeta(meta);
					}
				} catch(Throwable t) {
					log(LogLevel.ERROR, "Problem transforming treatments using " + transformer.getClass(), t);
					t.printStackTrace();
				}
			}
			
					
			log(LogLevel.DEBUG, "writing result using " + writer.getClass());
			writer.write(descriptionsFileList, outputDirectory);
			
			return new DescriptionMarkupResult(descriptionsFileList);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem reading, transforming or writing treatments", e);
		}
		return null;
	}

	public void add(IDescriptionTransformer descriptionTransformer) {
		this.descriptionTransformers.add(descriptionTransformer);
	}
	
	public boolean remove(IDescriptionTransformer descriptionTransformer) {
		return descriptionTransformers.remove(descriptionTransformer);
	}
	
}
