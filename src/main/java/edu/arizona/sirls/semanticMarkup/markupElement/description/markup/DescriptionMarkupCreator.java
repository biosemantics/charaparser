package edu.arizona.sirls.semanticMarkup.markupElement.description.markup;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.IDescriptionReader;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.IDescriptionWriter;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.CharaParser;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Meta;
import edu.arizona.sirls.semanticMarkup.markupElement.description.transform.AbstractDescriptionTransformer;
import edu.arizona.sirls.semanticMarkup.markupElement.description.transform.IDescriptionTransformer;
import edu.arizona.sirls.semanticMarkup.markupElement.description.transform.TransformationReport;

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
	
	@Inject
	public DescriptionMarkupCreator(@Named("DescriptionMarkupCreator_DescriptionReader") IDescriptionReader reader,	
			IDescriptionTransformer descriptionTransformer,
			@Named("DescriptionMarkupCreator_DescriptionWriter") IDescriptionWriter writer, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		this.reader = reader;
		this.descriptionTransformer = descriptionTransformer;
		this.writer = writer;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
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
				CharaParser charaParser = new CharaParser();
				charaParser.setCharaparserVersion(report.getCharaparserVersion());
				charaParser.setGlossaryName(report.getGlossaryType());
				charaParser.setGlossaryVersion(report.getGlossaryVersion());
				meta.addProcessedBy(charaParser);
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
