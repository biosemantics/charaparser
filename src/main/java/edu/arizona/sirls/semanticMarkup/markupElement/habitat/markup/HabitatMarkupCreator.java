package edu.arizona.sirls.semanticMarkup.markupElement.habitat.markup;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.IHabitatReader;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.IHabitatWriter;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.transform.HabitatTransformer;

/**
 * CharaParserMarkupCreator creates a markup by reading treatments, transforming them using a TreatmentTransformerChain and writing them out
 * @author thomas rodenhausen
 */
public class HabitatMarkupCreator implements IHabitatMarkupCreator {

	private IHabitatReader reader;
	private HabitatTransformer habitatTransformer;
	private IHabitatWriter writer;
	private String inputDirectory;
	private String outputDirectory;
	
	@Inject
	public HabitatMarkupCreator(@Named("HabitatMarkupCreator_Reader") IHabitatReader reader,	
			String inputDirectory,
			HabitatTransformer habitatTransformer,
			@Named("HabitatMarkupCreator_Writer") IHabitatWriter writer, 
			String outputDirectory) {
		this.reader = reader;
		this.inputDirectory = inputDirectory;
		this.habitatTransformer = habitatTransformer;
		this.writer = writer;
		this.outputDirectory = outputDirectory;
	}
	
	public HabitatMarkupResult create() {
		HabitatsFileList habitatsFileList = null;
		try {
			log(LogLevel.DEBUG, "reading treatments using " + reader.getClass());
			habitatsFileList = reader.read(inputDirectory);
			
			log(LogLevel.DEBUG, "transform treatments using " + habitatTransformer.getClass());
			habitatTransformer.transform(habitatsFileList);
			
			log(LogLevel.DEBUG, "writing result using " + writer.getClass());
			writer.write(habitatsFileList, outputDirectory);
			
			return new HabitatMarkupResult(habitatsFileList);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem reading transforming or writing habitats", e);
		}
		return new HabitatMarkupResult(habitatsFileList);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
}
